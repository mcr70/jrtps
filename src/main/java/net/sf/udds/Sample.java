package net.sf.udds;

/**
 * Sample
 * @author mcr70
 *
 * @param <T> type of the sample
 */
public interface Sample<T> {
	/**
	 * Get the associated data.
	 * @return
	 */
	public T getData();
	
	/**
	 * Get the SampleInfo
	 * @return
	 */
	public SampleInfo getSampleInfo();
}
