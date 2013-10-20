package net.sf.jrtps.udds;

import java.util.List;

/**
 * DataListener interface is used to get notifications of changes in associated DDS Topic.
 * 
 * @author mcr70
 *
 * @param <T> Type of the Objects received
 */
public interface DataListener<T> {
	/**
	 * Called by DataReader to notify listeners of new samples of type T.
	 * @param samples
	 */
	public void onDataAvailable(List<T> samples);
}
