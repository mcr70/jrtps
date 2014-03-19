package net.sf.jrtps.rtps;

import java.util.LinkedList;


/**
 * WriterCache represents writers history cache from the RTPSWriter point of
 * view. RTPSWriter uses WriterCache to construct Data and HeartBeat messages to
 * be sent to RTPSReaders.
 * 
 * @author mcr70
 * 
 */
public interface WriterCache <T> {
    /**
     * Gets the smallest sequence number available in history cache.
     * 
     * @return seqNumMin
     */
    public long getSeqNumMin();

    /**
     * Gets the greatest sequence number available in history cache.
     * 
     * @return seqNumMax
     */
    public long getSeqNumMax();

    /**
     * Gets all the CacheChanges since given sequence number.
     * Returned CacheChanges are ordered by sequence numbers.
     * 
     * @param seqNum sequence number to compare
     * @return changes since given seqNum. Returned List is newly allocated.
     */
    public LinkedList<CacheChange<T>> getChangesSince(long seqNum);
}
