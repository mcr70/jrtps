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
     * @param sample 
     * @param timestamp
     */
    void dispose(T sample, long timestamp);
    /**
     * Unregister an instance represented by given sample.
     * @param sample
     * @param timestamp
     */
    void unregister(T sample, long timestamp);
    /**
     * Writes a Sample.
     * @param sample
     * @param timestamp
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
