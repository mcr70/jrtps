package net.sf.udds;

import java.util.List;

import net.sf.jrtps.RTPSWriter;
import net.sf.jrtps.RTPSWriter.ChangeKind;

/**
 * This class represents a strongly typed DataWriter in spirit of DDS specification.
 * 
 * @author mcr70
 *
 * @param <T> Type of the DataWriter. Type may be obtained from an external tool like 
 * IDL compiler, or it may be more dynamically constructed Object that is used with uDDS.
 */
public class DataWriter<T> extends Entity {
	private RTPSWriter rtps_writer;
	
	// TODO: Consider timestamp methods. At the moment they are left out. 
	//       Why would anyone want to fake a timestamp. It could cause more trouble than useful stuff.
	//	     Like order (timestamp point of view) of the messages could be wrong on receiving side. 
	
	/**
	 * Creates this DataWriter with given topic name.
	 * 
	 * @param topicName
	 */
	DataWriter(String topicName, RTPSWriter writer) {
		super(topicName);
		this.rtps_writer = writer;
	}
	
	/**
	 * Writes an instance to subscribed data readers.
	 * 
	 * @param instance
	 */
	public void write(T instance) {
		rtps_writer.createChange(instance);
		rtps_writer.sendHeartbeat();
	}

	/**
	 * Writes a List of instances to subscribed data readers. 
	 * 
	 * @param instance
	 */
	public void write(List<T> instances) {
		for (T t : instances) { // TODO: this loop should be moved to HistoryCache for synchronization purposes
			rtps_writer.createChange(t);
		}
		
		rtps_writer.sendHeartbeat();
	}

	/**
	 * Dispose a given instance.
	 * @param instance
	 */
	public void dispose(T instance) {
		// TODO: see 8.7.4 Changes in the Instance Lifecycle State
		//       see 9.6.3.4 StatusInfo_t (PID_STATUS_INFO)
		rtps_writer.createChange(ChangeKind.DISPOSE, instance);
		rtps_writer.sendHeartbeat();
	}
	
	/**
	 * Dispose a List of instances.
	 * @param instances
	 */
	public void dispose(List<T> instances) {
		for (T t : instances) { // TODO: this loop should be moved to HistoryCache for synchronization purposes
			rtps_writer.createChange(ChangeKind.DISPOSE, t);
		}
		
		rtps_writer.sendHeartbeat();
	}
}
