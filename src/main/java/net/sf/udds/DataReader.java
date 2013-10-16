package net.sf.udds;

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
	 * @return
	 */
	public List<Sample<T>> read() {
		//rtps_reader.getHistoryCache();
		
		LinkedList<Sample<T>> l = new LinkedList<>();
		return l;
	}
	
	/**
	 * Take samples.
	 * @return
	 */
	public List<Sample<T>> take() {
		return null;
	}

	public void addListener(DataListener<T> listener) {
		DataListenerAdapter<T> dla = new DataListenerAdapter<>(listener);
		rtps_reader.addListener(dla);
		
		dataListeners.add(dla);
	}
}
