package net.sf.udds;

import java.util.List;

/**
 * SampleListener interface is used to get notifications of changes in associated DDS Topic.
 * 
 * @author mcr70
 *
 * @param <T> Type of the Samples received from DDS
 */
public interface SampleListener<T> {
	public void onSamplesAvailable(List<Sample<T>> samples);
}
