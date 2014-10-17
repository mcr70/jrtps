package net.sf.jrtps.rtps;

import java.util.List;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReaderProxy represents a remote reader.
 * 
 * @author mcr70
 */
public class ReaderProxy extends RemoteProxy {
    private static final Logger log = LoggerFactory.getLogger(ReaderProxy.class);
    private final boolean expectsInlineQoS;

    private AckNack latestAckNack;
    private long readersHighestSeqNum = 0;
    private boolean active = true;
    private long heartbeatSentTime = 0; // set to 0 after acknack

	private long latestAckNackReceiveTime;

	private final int anSuppressionDuration;
    private final EntityId entityId;


    ReaderProxy(EntityId entityId, SubscriptionData rd, List<Locator> locators, int anSuppressionDuration) {
        super(rd, locators);
        this.entityId = entityId;
        this.expectsInlineQoS = rd.expectsInlineQos(); 
		this.anSuppressionDuration = anSuppressionDuration;
    }

    /**
     * Gets the ReaderData associated with this ReaderProxy.
     * 
     * @return ReaderData
     */
    public SubscriptionData getSubscriptionData() {
        return (SubscriptionData) getDiscoveredData();
    }

    /**
     * Returns true if remote reader expects QoS to be sent inline with each
     * Data submessage.
     * 
     * @return true or false
     */
    boolean expectsInlineQoS() {
        return expectsInlineQoS;
    }

    long getReadersHighestSeqNum() {
        return readersHighestSeqNum;
    }

    void setReadersHighestSeqNum(long l) {
        this.readersHighestSeqNum = l;
    }

    boolean isActive() {
        return active;
    }

    void heartbeatSent() {
        if (heartbeatSentTime != 0) {
            this.heartbeatSentTime = System.currentTimeMillis();
        } else {
            active = false;
        }
    }

    int getLatestAckNackCount() {
        if (latestAckNack == null) {
            return 0;
        }

        return latestAckNack.getCount();
    }

    /**
     * Updates proxys latest AckNack. Latest AckNack gets updated only if its
     * count is greater than previously received AckNack. This ensures, that
     * AckNack gets processed only once.
     * 
     * @param ackNack
     * @return true, if AckNack was accepted
     */
    boolean ackNackReceived(AckNack ackNack) {
        long anReceiveTime = System.currentTimeMillis();
        
        // First AN is always accepted
        if (latestAckNack == null) {
            latestAckNack = ackNack;
            latestAckNackReceiveTime = anReceiveTime;

            return true;
        }

    	// Accept only if count > than previous, and enough time (suppression duration) has
    	// elapsed since previous AN
        if (ackNack.getCount() > latestAckNack.getCount() && 
        		anReceiveTime > latestAckNackReceiveTime + anSuppressionDuration) {
        	latestAckNack = ackNack;
            latestAckNackReceiveTime = anReceiveTime;

            return true;
        }

        log.debug("[{}] AckNack was not accepted; count {} < proxys count {}, or suppression duration not elapsed; {} < {}", 
        		entityId, ackNack.getCount(), latestAckNack.getCount(), anReceiveTime, latestAckNackReceiveTime + anSuppressionDuration);
        
        return false;
    }
}
