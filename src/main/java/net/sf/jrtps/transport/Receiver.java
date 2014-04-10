package net.sf.jrtps.transport;

/**
 * Receiver will do its work during run() method.
 * 
 * @author mcr70
 */
public interface Receiver extends Runnable {
    /**
     * Close this Receiver
     */
    void close();
}
