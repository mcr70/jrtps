package net.sf.jrtps;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.AckNack;

/**
 * ReaderProxy represents a remote reader.
 * 
 * @author mcr70
 */
public class ReaderProxy extends RemoteProxy {
    private final boolean expectsInlineQoS;

    private AckNack latestAckNack;
    private long readersHighestSeqNum = 0;
    private boolean active = true;
    private long heartbeatSentTime = 0; // set to 0 after acknack

    ReaderProxy(SubscriptionData readerData, LocatorPair locators) {
        this(readerData, locators, false);
    }

    ReaderProxy(SubscriptionData rd, LocatorPair lPair, boolean expectsInlineQoS) {
        super(rd, lPair.ucLocator, lPair.mcLocator);
        this.expectsInlineQoS = expectsInlineQoS;
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
        if (latestAckNack == null) {
            latestAckNack = ackNack;
            return true;
        }

        if (ackNack.getCount() > latestAckNack.getCount()) {
            latestAckNack = ackNack;
            return true;
        }

        return false;
    }
}
