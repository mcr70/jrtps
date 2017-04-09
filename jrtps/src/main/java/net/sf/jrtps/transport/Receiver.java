package net.sf.jrtps.transport;

/**
 * Receiver will be used to receive packets from the source. Typically, source is from the network, but
 * it can be anything. Like memory, file etc. Receiver will do its work during run() method.
 * Receivers thread is controlled by Participants Thread pool. When Participant is closing,
 * it will call TransportProviders' close() method to do graceful shutdown and resource cleanup.
 * 
 * @author mcr70
 * @see Transmitter
 * @see TransportProvider
 */
public interface Receiver extends Runnable {
}
