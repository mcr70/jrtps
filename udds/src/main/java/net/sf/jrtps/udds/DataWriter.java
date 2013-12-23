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
public class DataWriter<T> extends Entity {
	private RTPSWriter<T> rtps_writer;
	
	// TODO: Consider timestamp methods. At the moment they are left out. 
	//       Why would anyone want to fake a timestamp. It could cause more trouble than useful stuff.
	//	     Like order (timestamp point of view) of the messages could be wrong on receiving side. 
	
	/**
	 * Creates this DataWriter with given topic name.
	 * 
	 * @param topicName
	 */
	DataWriter(Participant p, RTPSWriter<T> writer) {
		super(p, writer.getTopicName());
		this.rtps_writer = writer;
	}
	
	/**
	 * Writes an instance to subscribed data readers.
	 * 
	 * @param instance
	 */
	public void write(T instance) {
		LinkedList<T> ll = new LinkedList<>();
		ll.add(instance);
		rtps_writer.write(ll);
	}

	/**
	 * Writes a List of instances to subscribed data readers. 
	 * 
	 * @param instances a List of instances
	 */
	public void write(List<T> instances) {
		rtps_writer.write(instances);
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
		rtps_writer.dispose(ll);
	}
	
	/**
	 * Dispose a List of instances.
	 * @param instances
	 */
	public void dispose(List<T> instances) {
		rtps_writer.dispose(instances);
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
