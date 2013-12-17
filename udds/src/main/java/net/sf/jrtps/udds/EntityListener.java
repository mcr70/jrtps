package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;

/**
 * EntityListener.
 * @author mcr70
 */
public interface EntityListener {
	/**
	 * Called when a liveliness of remote writer has been lost.
	 * @param remoteWriter
	 */
	public void livelinessLost(WriterData remoteWriter);
	
	/**
	 * Called when a liveliness of remote reader has been lost.
	 * Note, that there is no mechanism built into protocol for the liveliness
	 * of the readers. This method is called when jRTPS thinks remote reader 
	 * is not reachable anymore. This could happen for example, when reader does not
	 * respond to writer in timely manner.
	 * 
	 * @param remoteReader
	 */
	public void livelinessLost(ReaderData remoteReader);

	/**
	 * Called when a new remote reader has been detected.
	 * @param rd
	 */
	public void readerDetected(ReaderData rd);

	/**
	 * Called when a local writer is associated with remote reader 
	 * @param writer local writer
	 * @param rd remote reader
	 */
	public void readerMatched(DataWriter<?> writer, ReaderData rd);

	/**
	 * Called when a new remote writer has been detected.
	 * @param wd
	 */
	public void writerDetected(WriterData wd);	
	
	/**
	 * Called when a local reader is associated with remote writer 
	 * @param reader local reader
	 * @param wd remote writer
	 */
	public void writerMatched(DataReader<?> reader, WriterData wd);

	/**
	 * Called when a local writer cannot be associated with remote reader because of 
	 * inconsistent QoS. 
	 * @param writer local writer
	 * @param rd remote reader
	 */
	public void inconsistentQoS(DataWriter<?> writer, ReaderData rd);

	/**
	 * Called when a local reader cannot be associated with remote writer because of 
	 * inconsistent QoS. 
	 * @param reader local reader
	 * @param wd remote writer
	 */
	public void inconsistentQoS(DataReader<?> reader, WriterData wd);
}
