package net.sf.jrtps;

import java.util.SortedSet;

import net.sf.jrtps.rtps.CacheChange;

/**
 * WriterCache represents writers history cache from the RTPSWriter point of
 * view. RTPSWriter uses WriterCache to construct Data and HeartBeat messages to
 * be sent to RTPSReaders.
 * 
 * @author mcr70
 * 
 */
public interface WriterCache {
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
     * 
     * @param seqNum
     *            Highest sequence number reader has
     * @return SortedSet
     */
    public SortedSet<CacheChange> getChangesSince(long seqNum);
}
