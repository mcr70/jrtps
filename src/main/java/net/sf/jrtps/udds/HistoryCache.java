package net.sf.jrtps.udds;

import java.util.Set;

/**
 * HistoryCache represents a uDDS history cache. 
 *
 * @author mcr70
 */
public interface HistoryCache<T> {
    /**
     * Dispose an instance represented by given sample
     * @param sample  Sample to dispose
     * @param timestamp timestamp of the disposal
     * @param coherent whether or not this dispose is part of a coherent set of changes 
     */
    void dispose(T sample, long timestamp, boolean coherent);
    /**
     * Unregister an instance represented by given sample.
     * @param sample Sample representing instance to unregister
     * @param timestamp timestamp of unregister
     * @param coherent whether or not this unregister is part of a coherent set of changes 
     */
    void unregister(T sample, long timestamp, boolean coherent);
    /**
     * Writes a Sample.
     * @param sample Sample to write
     * @param timestamp timestamp of the write
     * @param coherent whether or not this write is part of a coherent set of changes 
     */
    void write(T sample, long timestamp, boolean coherent);
    
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
