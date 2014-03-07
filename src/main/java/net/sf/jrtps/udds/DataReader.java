package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.Map;

import net.sf.jrtps.rtps.RTPSReader;

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
    private Map<SampleListener<T>, RTPSListenerAdapter<T>> adapters = new HashMap<>();
    private RTPSReader<T> rtps_reader;

    /**
     * Package access. This class is only instantiated by Participant class.
     * 
     * @param topicName
     */
    DataReader(Participant p, Class<T> type, RTPSReader<T> reader) {
        super(p, type, reader.getTopicName());
        this.rtps_reader = reader;
    }


    /**
     * Adds a SampleListener to this DataReader
     * @param listener Listener to add
     */
    public void addListener(SampleListener<T> listener) {
        synchronized (adapters) {
            RTPSListenerAdapter<T> adapter = new RTPSListenerAdapter<>(listener);
            adapters.put(listener, adapter);
            rtps_reader.addListener(adapter);            
        }
    }

    /**
     * Removes a given SampleListener from this DataReader.
     * @param listener A listener to remove
     */
    public void removeListener(SampleListener<T> listener) {
        synchronized (adapters) {
            RTPSListenerAdapter<T> adapter = adapters.remove(listener);
            rtps_reader.removeListener(adapter);
        }
    }

    /**
     * Package access
     */
    RTPSReader<T> getRTPSReader() {
        return rtps_reader;
    }
}
