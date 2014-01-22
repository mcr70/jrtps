package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.RTPSWriter;
import net.sf.jrtps.types.Guid;



/**
 * This class represents a strongly typed DataWriter in spirit of DDS specification.
 * 
 * @author mcr70
 *
 * @param <T> Type of the DataWriter. Type may be obtained from an external tool like 
 * IDL compiler, or it may be more dynamically constructed Object that is used with uDDS.
 */
public class DataWriter<T> extends Entity<T> {
	private final RTPSWriter<T> rtps_writer;
	private final HistoryCache<T> hCache;
	
	/**
	 * Creates this DataWriter with given topic name.
	 * 
	 * @param topicName
	 */
	DataWriter(Participant p, Class<T> type, RTPSWriter<T> writer, HistoryCache<T> hCache) {
		super(p, type, writer.getTopicName());
		this.rtps_writer = writer;
		this.hCache = hCache;
		
		hCache.setDataWriter(this);
	}
	
	/**
	 * Writes an instance to subscribed data readers.
	 * 
	 * @param instance
	 */
	public void write(T instance) {
		LinkedList<T> ll = new LinkedList<>();
		ll.add(instance);
		write(ll);
	}

	/**
	 * Writes a List of instances to subscribed data readers. 
	 * 
	 * @param instances a List of instances
	 */
	public void write(List<T> instances) {
		try {
			hCache.write(instances);
		}
		finally {
			notifyReaders();
		}
	}

	/**
	 * Dispose a given instance.
	 * @param instance
	 */
	public void dispose(T instance) {
		// TODO: see 8.7.4 Changes in the Instance Lifecycle State
		//       see 9.6.3.4 StatusInfo_t (PID_STATUS_INFO)
		LinkedList<T> ll = new LinkedList<>();
		ll.add(instance);
		dispose(ll);
	}
	
	/**
	 * Dispose a List of instances.
	 * @param instances
	 */
	public void dispose(List<T> instances) {
		try {
			hCache.dispose(instances);
		}
		finally {
			notifyReaders();
		}
	}


	RTPSWriter<T> getRTPSWriter() {
		return rtps_writer;
	}


	/**
	 * Gets the Guid of thie DataWriter
	 * @return Guid
	 */
	Guid getGuid() {
		return rtps_writer.getGuid();
	}

	/**
	 * Notifies readers of the changes available.
	 */
	void notifyReaders() {
		rtps_writer.notifyReaders();
	}
}
