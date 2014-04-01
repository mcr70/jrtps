package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.SubscriptionData;

/**
 * ReaderListener can be added to DataWriter. This can be used to track remote readers 
 * when they can / cannot be matched with local DataWriter.
 * 
 * @author mcr70
 * @see DataWriter#addReaderListener(ReaderListener)
 */
public interface ReaderListener {
    /**
     * This method is called when a remote reader has been successfully matched. 
     * @param sd SubscriptionData of the remote reader
     */
    void readerMatched(SubscriptionData sd);

    /**
     * This method is called when a remote reader cannot be matched with DataWriter due to 
     * inconsistent QualityOfService. 
     * @param sd SubscriptionData of the remote reader
     */
    void inconsistentQoS(SubscriptionData sd);
}
