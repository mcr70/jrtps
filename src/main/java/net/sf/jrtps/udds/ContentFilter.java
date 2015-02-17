package net.sf.jrtps.udds;

import net.sf.jrtps.message.parameter.ContentFilterProperty;

/**
 * ContentFilter interface provides ContentFilterProperty which is transferred 
 * to writers.
 * 
 * @author mcr70
 *
 * @param <T>
 */
public interface ContentFilter<T> extends SampleFilter<T> {
	/**
	 * Gets a ContentFilterProperty that will be sent to writers
	 * @return ContentFilterProperty
	 */
	ContentFilterProperty getContentFilterProperty();
}
