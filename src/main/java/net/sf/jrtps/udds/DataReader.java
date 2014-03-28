package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.Sample;

/**
 * This class represents a strongly typed DataReader in spirit of DDS
 * specification.
 * 
 * @author mcr70
 * 
 * @param <T>
 *            Type of the DataReader. Type may be obtained from an external tool
 *            like IDL compiler, or it may be more dynamically constructed
 *            Object that is used with uDDS.
 */
public class DataReader<T> extends Entity<T> {
    private final RTPSReader<T> rtps_reader;
    private final HistoryCache<T> hCache;

    /**
     * Package access. This class is only instantiated by Participant class.
     * 
     * @param topicName
     */
    DataReader(Participant p, Class<T> type, RTPSReader<T> reader, HistoryCache<T> hCache) {
        super(p, type, reader.getTopicName());
        this.rtps_reader = reader;
        this.hCache = hCache;
    }


    /**
     * Adds a SampleListener to this DataReader
     * @param listener Listener to add
     */
    public void addListener(SampleListener<T> listener) {
        hCache.addListener(listener);
    }

    /**
     * Removes a given SampleListener from this DataReader.
     * @param listener A listener to remove
     */
    public void removeListener(SampleListener<T> listener) {
        hCache.removeListener(listener);
    }
    
    /**
     * Package access
     */
    RTPSReader<T> getRTPSReader() {
        return rtps_reader;
    }

    // ----  Experimental code follows  ------------------------
    /**
     * Adds a reader side Filter. When samples are received, they are evaluated with
     * all the Filters this DataReader has. If a Sample is accepted by all of the Filters,
     * it is added to history cache of this reader, and clients are notified of new samples.
     *  
     * @param filter
     */
    void addFilter(SampleFilter<T> filter) {
        // QosOwnership could be implemented with Filters.
        // QosResourceLimits could be implemented with Filters.
    }
    
    /**
     * Gets samples that match Filter. History cache is scanned through and for each
     * Sample, a Filter is applied. Only the accepted Samples are returned.
     * 
     * @param filter
     * @return A List of Samples that matched given Filter
     */
    List<Sample<T>> getSamples(SampleFilter<T> filter) {
        List<Sample<T>> filteredSampled = new LinkedList<>();
        
        List<Sample<T>> samples = getSamples();
        for (Sample<T> sample : samples) {
            if (filter.acceptSample(sample)) {
                filteredSampled.add(sample);
            }
        }
        
        return filteredSampled;
    }

    
    /**
     * Gets a Set of instances this DataReader knows. Each Sample returned
     * is the latest sample of that instance.
     * 
     * @return a Set of instances
     */
    public Set<Instance<T>> getInstances() {
        return hCache.getInstances();
    }

    /**
     * Gets an Instance with given key. Key of the Instance may be obtained from Sample.
     * @param key KeyHash of the Instance
     * @return Instance with given key, or null if there is no such Instance available
     * @see Sample#getKey()
     */
    public Instance<T> getInstance(KeyHash key) {
        return hCache.getInstance(key);
    }
    
    
    /**
     * Gets all the samples this DataReader knows about. Samples are returned in the order
     * they have been received.
     * 
     * @return all the Samples
     */
    public List<Sample<T>> getSamples() {
        return hCache.getSamplesSince(0);
    }
    
    /**
     * Gets all the Samples that have been received after given Sample.
     * @param s
     * @return all the samples that have been received after given Sample
     */
    public List<Sample<T>> getSamplesSince(Sample<T> s) {
        return hCache.getSamplesSince(s.getSequenceNumber());
    }

    List<Sample<T>> takeSamples() {
        return null; // NOT TO BE IMPLEMENTED
        // trying to avoid read/take semantics
    }
    // ----  End of experimental code
}
