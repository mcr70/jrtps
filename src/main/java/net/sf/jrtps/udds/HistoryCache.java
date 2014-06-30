package net.sf.jrtps.udds;

import java.util.Set;

/**
 * HistoryCache represents a uDDS history cache. 
 *
 * @author mcr70
 */
public interface HistoryCache<T> {
    /**
     * Calling this method starts a new coherent set.
     */
    void coherentChangesBegin();
    
    /**
     * Calling this method ends current coherent set.
     */
    void coherentChangesEnd();
    
    /**
     * Dispose an instance represented by given sample
     * @param sample  Sample to dispose
     * @param timestamp timestamp of the disposal
     */
    void dispose(T sample, long timestamp);
    /**
     * Unregister an instance represented by given sample.
     * @param sample Sample representing instance to unregister
     * @param timestamp timestamp of unregister
     */
    void unregister(T sample, long timestamp);
    /**
     * Writes a Sample.
     * @param sample Sample to write
     * @param timestamp timestamp of the write
     */
    void write(T sample, long timestamp);
    
    /**
     * Registers an instance represented by given sample.
     * @param sample
     * @param timestamp
     * @return an Instance
     */
    Instance<T> register(T sample, long timestamp);

    /**
     * Gets instances of this HistoryCache.
     * @return a Set of instances
     */
    Set<Instance<T>> getInstances();
}
