package alt.rtps.behavior;

import alt.rtps.structure.CacheChange;

public class ChangeFromWriter {
	CacheChange cache_change;
	
	/**
	 * Indicates whether the change is relevant to the RTPS Reader.<p>
	 * The determination of irrelevant changes is affected by DDS DataReader
	 * TIME_BASED_FILTER QoS and also by the use of DDS ContentFilteredTopics.
	 */
	boolean isRelevant = true;
	
	/**
	 * Indicates the status of a CacheChange relative to the RTPS Writer represented by the WriterProxy.
	 */
	ChangeFromWriterStatusKind status;
}
