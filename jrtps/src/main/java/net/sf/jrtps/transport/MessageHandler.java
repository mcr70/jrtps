package net.sf.jrtps.transport;

import net.sf.jrtps.message.Message;

/**
 * This interface is used by receiver.
 * @author mcr70
 *
 */
public interface MessageHandler {
	/**
	 * Handle given RTPS Message.
	 * @param msg
	 */
	public void handleMessage(Message msg);
}
