package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.RTPSReader;

/**
 * This class represents a strongly typed DataReader in spirit of DDS specification.
 * 
 * @author mcr70
 *
 * @param <T> Type of the DataReader. Type may be obtained from an external tool like 
 * IDL compiler, or it may be more dynamically constructed Object that is used with uDDS.
 */
public class DataReader<T> extends Entity {
	private List<DataListenerAdapter<T>> dataListeners = new LinkedList<>();
	private RTPSReader rtps_reader;

	/**
	 * Package access. This class is only instantiated by Participant class.
	 * @param topicName
	 */
	DataReader(String topicName, RTPSReader reader) {
		super(topicName);
		this.rtps_reader = reader;
	}
	
	/**
	 * Read samples.
	 * 
	 * @return a List of Sample<T>
	 */
	public List<Sample<T>> read() {
		//rtps_reader.getHistoryCache();
		
		LinkedList<Sample<T>> l = new LinkedList<>();
		return l;
	}
	
	/**
	 * Take samples.
	 * @return a List of Sample<T>
	 */
	public List<Sample<T>> take() {
		return null;
	}

	/**
	 * Adds a new listener for this DataReader.
	 * 
	 * @param listener a DataListener to add.
	 */
	public void addListener(DataListener<T> listener) {
		DataListenerAdapter<T> dla = new DataListenerAdapter<>(listener);
		rtps_reader.addListener(dla);
		
		dataListeners.add(dla);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener DataListener to remove
	 */
	public void removeListener(DataListener<T> listener) {
		for (DataListenerAdapter<T> dla : dataListeners) {
			if (dla.udds_listener.equals(listener)) {
				rtps_reader.removeListener(dla);
				dataListeners.remove(dla);
			}
		}
	}
}
