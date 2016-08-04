package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.Set;

import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.Sample;

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
     * @param sample Sample of type T
     * @param timestamp timestamp
     * 
     * @return an Instance
     */
    Instance<T> register(T sample, long timestamp);

    /**
     * Gets instances of this HistoryCache.
     * @return a Set of instances
     */
    Set<Instance<T>> getInstances();
    
    /**
     * Get an Instance represented by given Key
     * @param key KeyHash representing an instance
     * @return Instance, or null if there was not Instance with given key
     */
    Instance<T> getInstance(KeyHash key);
    
    /**
     * Gets all the CacheChanges since given sequence number.
     * Returned CacheChanges are ordered by sequence numbers.
     * 
     * @param seqNum sequence number to compare
     * @return changes since given seqNum. Returned List is newly allocated.
     */
    LinkedList<Sample<T>> getSamplesSince(long seqNum); // TODO: this should be removed    

    /**
     * Close this HistoryCache.
     */
    void close();
}
