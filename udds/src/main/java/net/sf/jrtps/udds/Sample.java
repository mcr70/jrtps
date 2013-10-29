package net.sf.jrtps.udds;

/**
 * Sample
 * @author mcr70
 *
 * @param <T> type of the sample
 */
public interface Sample<T> {
	/**
	 * Get the associated data.
	 * @return T
	 */
	public T getData();
	
	/**
	 * Get the SampleInfo
	 * @return SampleInfo
	 */
	public SampleInfo getSampleInfo();
}
