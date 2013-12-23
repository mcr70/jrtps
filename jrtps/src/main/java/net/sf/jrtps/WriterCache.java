package net.sf.jrtps;

import java.util.SortedSet;

/**
 * WriterCache represents writers history cache from the RTPSWriter point of view.
 * 
 * @author mcr70
 *
 */
public interface WriterCache {
	/**
	 * Gets the smallest sequence number available in history cache.
	 * @return seqNumMin
	 */
	public long getSeqNumMin();

	/**
	 * Gets the greatest sequence number available in history cache.
	 * @return seqNumMax
	 */
	public long getSeqNumMax();

	/**
	 * Gets all the CacheChanges since given sequence number.
	 * @param seqNum Highest sequence number reader has
	 * @return SortedSet
	 */
	public SortedSet<CacheChange> getChangesSince(long seqNum);
}
