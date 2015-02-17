package net.sf.jrtps.udds;

import net.sf.jrtps.rtps.Sample;

/**
 * A SampleFilter is used to filter incoming samples at reader.
 * 
 * @author mcr70
 *
 * @param <T>
 */
interface SampleFilter<T> {
	/**
	 * This method is called to determine whether or not an incoming
	 * Sample should be added to readers history cache.
	 * 
	 * @param sample incoming Sample
	 * @return true or false
	 */
    boolean acceptSample(Sample<T> sample);
}
