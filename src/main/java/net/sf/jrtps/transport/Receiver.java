package net.sf.jrtps.transport;

import net.sf.jrtps.types.Locator;

/**
 * Receiver will be used to receive packets from the source. Typically, source is from the network, but
 * it can be anything. Like memory, file etc. Receiver will do its work during run() method.
 * 
 * @author mcr70
 * @see Transmitter
 * @see TransportProvider
 */
public interface Receiver extends Runnable {
    /**
     * Gets the locator associated with this Receiver. This locator will be transmitted 
     * to remote participants.
     * @return Locator
     */
    Locator getLocator();
    /**
     * Close this Receiver
     */
    void close();
}
