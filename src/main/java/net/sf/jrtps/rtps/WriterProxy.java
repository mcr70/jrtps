package net.sf.jrtps.rtps;

import java.util.List;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.Gap;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.util.Watchdog.Task;

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

    private final RTPSReader<?> reader;
    private final int hbSuppressionDuration;
    
    private Heartbeat latestHeartbeat;
    private long latestHBReceiveTime;

    private volatile long seqNumMax = 0;
    private Task livelinessTask;
    
    private boolean isAlive = true; // reflects status of liveliness


    WriterProxy(RTPSReader<?> reader, PublicationData wd, List<Locator> locators, int heartbeatSuppressionDuration) {
        super(wd, locators);
        this.reader = reader;
		
        this.hbSuppressionDuration = heartbeatSuppressionDuration;
    }

    
    void setLivelinessTask(Task livelinessTask) {
        this.livelinessTask = livelinessTask;
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
	 * Checks, if all the samples from remote writer is already received or not.
	 * @return true, if every sample is received
	 */
    boolean isAllReceived() {
        if (latestHeartbeat == null) {
            return false;
        }

        return latestHeartbeat.getLastSequenceNumber() == getGreatestDataSeqNum();
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
        // TODO:
        // Data for reliable readers must come in order. If not, drop it. 
        // Manage out-of-order data with HeartBeat & AckNack & Gap messages

        if (sequenceNumber >= seqNumMax) {
            if (sequenceNumber > seqNumMax + 1 && seqNumMax != 0) {
                log.warn("Accepting data even though some data has been missed: offered seq-num {}, my received seq-num {}",
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
        if (!isAlive) {
            reader.notifyLivelinessRestored(getPublicationData());
        }
        
        isAlive = true;
        if (livelinessTask != null) {
            livelinessTask.reset();
        }
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
    	if (latestHeartbeat == null) {
            latestHeartbeat = hb;
            latestHBReceiveTime = hbReceiveTime;
            return true;
        }

    	// Accept only if count > than previous, and enough time (suppression duration) has
    	// elapsed since previous HB
        if (hb.getCount() > latestHeartbeat.getCount() && 
        		hbReceiveTime > latestHBReceiveTime + hbSuppressionDuration) {
            latestHeartbeat = hb;
            latestHBReceiveTime = hbReceiveTime;
            return true;
        }

        log.debug("Heartbeat was not accepted; count {} < proxys count {}, or suppression duration not elapsed; {} < {}", 
        		hb.getCount(), latestHeartbeat.getCount(), hbReceiveTime, latestHBReceiveTime + hbSuppressionDuration);

        return false;
    }

    void applyGap(Gap gap) {
        SequenceNumberSet gapList = gap.getGapList();
        long bitmapBase = gapList.getBitmapBase();
        if (bitmapBase - 1 > seqNumMax) {
            seqNumMax = bitmapBase - 1;
        }
    }

    void isAlive(boolean livelinessFlag) {
        this.isAlive = livelinessFlag;
    }
    public boolean isAlive() {
        return isAlive;
    }

    public String toString() {
        return getGuid().toString();
    }


}
