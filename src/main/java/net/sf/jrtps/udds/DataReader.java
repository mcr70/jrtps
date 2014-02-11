package net.sf.jrtps.udds;

import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.SampleListener;

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
     * Adds a new listener for this DataReader.
     * 
     * @param listener
     *            a DataListener to add.
     */
    public void addListener(SampleListener<T> listener) {
        rtps_reader.addListener(listener);
    }

    /**
     * Removes a listener.
     * 
     * @param listener
     *            DataListener to remove
     */
    public void removeListener(SampleListener<T> listener) {
        rtps_reader.removeListener(listener);
    }

    RTPSReader<T> getRTPSReader() {
        return rtps_reader;
    }
}
