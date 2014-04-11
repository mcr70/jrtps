package net.sf.jrtps.transport;

import net.sf.jrtps.types.Locator;

/**
 * Receiver will do its work during run() method.
 * 
 * @author mcr70
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
