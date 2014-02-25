package net.sf.jrtps.rtps;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.Gap;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.types.SequenceNumberSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WriterProxy represents a remote writer.
 * 
 * @author mcr70
 * 
 */
public class WriterProxy extends RemoteProxy {
    private static final Logger log = LoggerFactory.getLogger(WriterProxy.class);

    private Heartbeat latestHeartBeat;

    private volatile long livelinessTimestamp;
    private volatile long seqNumMax = 0;

	private final int hbSuppressionDuration;
	private long latestHeartBeatReceiveTime;


    WriterProxy(PublicationData wd, LocatorPair lPair, int heartbeatSuppressionDuration) {
        super(wd, lPair.ucLocator, lPair.mcLocator);
		hbSuppressionDuration = heartbeatSuppressionDuration;
    }

    /**
     * Gets the max Data seqnum that has been received.
     * 
     * @return max Data seqnum that has been received.
     */
    long getGreatestDataSeqNum() {
        return seqNumMax;
    }

    /**
	 * 
	 */
    boolean isAllReceived() {
        if (latestHeartBeat == null) {
            return false;
        }

        return latestHeartBeat.getLastSequenceNumber() == getGreatestDataSeqNum();
    }

    /**
     * Gets the PublicationData associated with this WriterProxy.
     * 
     * @return PublicationData
     */
    public PublicationData getPublicationData() {
        return (PublicationData) getDiscoveredData();
    }

    /**
     * Determines if incoming Data should be accepted or not.
     * 
     * @param sequenceNumber
     * @return true, if data was added to cache
     */
    boolean acceptData(long sequenceNumber) {
        // Data must come in order. If not, drop it. Manage out-of-order data
        // with
        // HeartBeat & AckNack messages

        // TODO: SPDP writer keeps its Data seq-num always at 1. We need >=
        // comparison here for that purpose.
        // So that participant lease expiration gets handled.
        if (sequenceNumber >= seqNumMax) {
            if (sequenceNumber > seqNumMax + 1 && seqNumMax != 0) {
                log.warn(
                        "Accepting data even though some data has been missed: offered seq-num {}, my received seq-num {}",
                        sequenceNumber, seqNumMax);
            }

            seqNumMax = sequenceNumber;

            return true;
        }

        return false;
    }

    /**
     * Asserts liveliness of a writer represented by this WriterProxy. Asserting
     * a liveliness marks remote writer as being 'alive'. This method should not
     * be called by user applications.
     */
    public void assertLiveliness() {
        livelinessTimestamp = System.currentTimeMillis();
    }

    /**
     * Updates proxys latest Heartbeat. Latest Heartbeat gets updated only if
     * its count is greater than previously received Heartbeat. This ensures,
     * that Heartbeat gets processed only once.
     * 
     * @param hb
     * @return true, if Heartbeat was accepted
     */
    boolean heartbeatReceived(Heartbeat hb) {
        long hbReceiveTime = System.currentTimeMillis();
        
        // First HB is always accepted
    	if (latestHeartBeat == null) {
            latestHeartBeat = hb;
            latestHeartBeatReceiveTime = hbReceiveTime;
            return true;
        }

    	// Accept only if count > than previous, and enough time (suppression duration) has
    	// elapsed since previous HB
        if (hb.getCount() > latestHeartBeat.getCount() && 
        		hbReceiveTime > latestHeartBeatReceiveTime + hbSuppressionDuration) {
            latestHeartBeat = hb;
            latestHeartBeatReceiveTime = hbReceiveTime;
            return true;
        }

        return false;
    }

    public String toString() {
        return getGuid().toString();
    }

    void applyGap(Gap gap) {
        SequenceNumberSet gapList = gap.getGapList();
        long bitmapBase = gapList.getBitmapBase();
        if (bitmapBase - 1 > seqNumMax) {
            seqNumMax = bitmapBase - 1;
        }
    }

    int getLatestHeartbeatCount() {
        if (latestHeartBeat == null) {
            return 0;
        }

        return latestHeartBeat.getCount();
    }
}
