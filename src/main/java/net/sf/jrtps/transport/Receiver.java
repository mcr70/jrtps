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
     * Gets the participantId associated with this receiver. During creation of receiver,
     * participantId may be given as -1, indicating that provider should generate one.
     * This method returns the value assigned by the provider.
     * 
     * @return participantId
     */
    int getParticipantId();
    
    /**
     * Close this Receiver
     */
    void close();
}
