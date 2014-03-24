package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

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
     * Gets a List of instances this DataReader knows. Each Sample returned
     * is the latest sample of that instance.
     * 
     * @return a List of instances
     */
    List<Sample<T>> getInstances() {
        return null;
    }
    /**
     * Gets the latest Sample of given instance.
     * @param s 
     * @return Latest Sample
     */
    Sample<T> getInstance(Sample<T> s) {
        return getInstanceHistory(s).get(0);
    }
    /**
     * Gets a history of given instance. If this DataReader is associated with a topic
     * that has no key, all the samples are returned. 
     * 
     * @param s A Sample representing an instance. 
     * @return a history of an instance. In returned List, index 0 represents most recent Sample.
     */
    List<Sample<T>> getInstanceHistory(Sample<T> s) {
        return null;
    }
    /**
     * Gets all the samples this DataReader knows about. Samples are returned in the order
     * they have been received.
     * @return all the Samples
     */
    List<Sample<T>> getSamples() {
        return getSamplesSince(0);
    }
    List<Sample<T>> getSamplesSince(long timeMillis) {
        return null;
    }
    List<Sample<T>> getSamplesSince(Sample<T> s) {
        return getSamplesSince(s.getTimestamp());
    }
    
    

    List<Sample<T>> takeSamples() {
        return null;
    }
    // ----  End of experimental code
}
