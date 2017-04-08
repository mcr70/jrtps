package net.sf.jrtps.rtps;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.Gap;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.util.Watchdog.Task;

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
    private final EntityId entityId;
    private final boolean isReliable;
    
    private Heartbeat latestHeartbeat;
    private long latestHBReceiveTime;

    private volatile long seqNumMax = 0;
    private Task livelinessTask;
    
    private boolean isAlive = true; // reflects status of liveliness

    private int strength;


    WriterProxy(RTPSReader<?> reader, PublicationData wd, List<Locator> locators, int heartbeatSuppressionDuration) {
        super(wd, locators);
        this.reader = reader;
        this.entityId = reader.getGuid().getEntityId();
        this.strength = wd.getQualityOfService().getOwnershipStrength().getStrength();		
        this.hbSuppressionDuration = heartbeatSuppressionDuration;
        
        this.isReliable = reader.getQualityOfService().getReliability().getKind().equals(QosReliability.Kind.RELIABLE);
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

        if (sequenceNumber > seqNumMax) {
            if (isReliable && sequenceNumber > seqNumMax + 1 && seqNumMax != 0) {
                log.warn("[{}] Accepting data even though some data has been missed: offered seq-num {}, my received seq-num {}",
                        entityId, sequenceNumber, seqNumMax);
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

        log.debug("[{}] Heartbeat was not accepted; count {} < proxys count {}, or suppression duration not elapsed; {} < {}", 
        		entityId, hb.getCount(), latestHeartbeat.getCount(), hbReceiveTime, latestHBReceiveTime + hbSuppressionDuration);

        return false;
    }

    void applyGap(Gap gap) {
    	// If the gap start is smaller than or equal to current seqNum + 1 (I.e. next seqNum)...
    	if (gap.getGapStart() <= seqNumMax + 1) {
    		long gapEnd = gap.getGapEnd();
    		// ...and gap end is greater than current seqNum...
    		if (gapEnd > seqNumMax) {
    			seqNumMax = gapEnd; // ...then mark current seqNum to be gap end.
    		}
    	}
    }

    SequenceNumberSet getSequenceNumberSet() {
    	long base = getGreatestDataSeqNum() + 1;
    	long firstSN = latestHeartbeat.getFirstSequenceNumber();
    	long lastSN = latestHeartbeat.getLastSequenceNumber();
    	int numBits; 
    	
    	if (base < firstSN) {
    		base = firstSN;
    		numBits = (int) (lastSN - firstSN + 1);
    	}
    	else {
    		numBits = (int) (lastSN - base + 1);
    	}
    	
    	if (numBits > 256) {
    		numBits = 256;
    	}
    	
    	return new SequenceNumberSet(base, numBits);
    }
    
    
    /**
     * Marks writer represented by this proxy as being alive or not.
     * @param b true if this writer is considered alive
     */
    public void isAlive(boolean b) {
        this.isAlive = b;
    }
    
    public boolean isAlive() {
        return isAlive;
    }

    public int getStrength() {
        return strength;
    }

    public String toString() {
        return getGuid().toString();
    }
}
