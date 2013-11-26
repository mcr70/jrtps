package net.sf.jrtps;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;

/**
 * EntityListener.
 * @author mcr70
 */
public interface EntityListener {
	/**
	 * Called when a liveliness of remote writer has been detected.
	 * @param remoteWriter
	 */
	public void livelinessLost(WriterData remoteWriter);
	
	/**
	 * Called when a local writer is associated with remote reader 
	 * @param writer local writer
	 * @param rd remote reader
	 */
	public void readerMatched(RTPSWriter<?> writer, ReaderData rd);

	/**
	 * Called when a local reader is associated with remote writer 
	 * @param reader local reader
	 * @param wd remote writer
	 */
	public void writerMatched(RTPSReader<?> reader, WriterData wd);

	/**
	 * Called when a local writer cannot be associated with remote reader because of 
	 * inconsistent QoS. 
	 * @param writer local writer
	 * @param rd remote reader
	 */
	public void inconsistentQoS(RTPSWriter<?> writer, ReaderData rd);

	/**
	 * Called when a local reader cannot be associated with remote writer because of 
	 * inconsistent QoS. 
	 * @param reader local reader
	 * @param wd remote writer
	 */
	public void inconsistentQoS(RTPSReader<?> reader, WriterData wd);
}
