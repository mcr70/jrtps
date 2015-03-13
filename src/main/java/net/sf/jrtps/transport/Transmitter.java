package net.sf.jrtps.transport;

import java.io.IOException;

import net.sf.jrtps.message.Message;

/**
 * Transmitter is used to deliver messages to destination.  
 * 
 * @author mcr70
 * @see Receiver
 * @see TransportProvider
 */
public interface Transmitter {
    /**
     * Sends a Message to destination.
     * @param msg Message to send
     * @return true, if an overflow occured.
     */
    public boolean sendMessage(Message msg);
    
    /**
     * Close this Transmitter
     * @throws IOException
     */
    public void close() throws IOException;
}
