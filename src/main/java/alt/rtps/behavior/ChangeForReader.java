package alt.rtps.behavior;

import alt.rtps.structure.CacheChange;


public class ChangeForReader {
	CacheChange cache_change;
	
	/**
	 * Indicates the status of a CacheChange relative to the RTPS Reader represented by the ReaderProxy.
	 */
	ChangeForReaderStatusKind status;
	
	/**
	 * Indicates whether the change is relevant to the RTPS Reader represented by the ReaderProxy.<p>
	 * 
	 * The determination of irrelevant changes is affected by DDS DataReader TIME_BASED_FILTER QoS
	 * and also by the use of DDS ContentFilteredTopics.
	 */
	boolean is_relevant = true;
}
