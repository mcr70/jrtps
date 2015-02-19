package net.sf.jrtps.udds;

import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.rtps.Sample;

/**
 * ContentFilter.
 * 
 * @author mcr70
 *
 * @param <T>
 */
public interface ContentFilter<T> {
	/**
	 * This method is called to determine whether or not a
	 * Sample should be passed forward. On reader side, this means that
	 * Sample is added to readers history cache. On writer side,
	 * this determines whether or not to send Sample to reader.
	 * 
	 * @param sample Sample
	 * @return true or false
	 */
    boolean acceptSample(Sample<T> sample);

	/**
	 * Gets a ContentFilterProperty that will be sent to writers
	 * @return ContentFilterProperty
	 */
	ContentFilterProperty getContentFilterProperty();
}
