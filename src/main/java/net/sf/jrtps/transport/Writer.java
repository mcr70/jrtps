package net.sf.jrtps.transport;

import java.io.IOException;

import net.sf.jrtps.message.Message;

/**
 * Writer.
 * @author mcr70
 */
public interface Writer {
    /**
     * Sends a Message to destination.
     * @param msg Message to send
     * @return true, if an overflow occured.
     */
    public boolean sendMessage(Message msg);
    
    /**
     * Close this Writer
     * @throws IOException
     */
    public void close() throws IOException;
}
