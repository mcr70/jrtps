package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.types.Guid;

/**
 * This class represents a strongly typed DataWriter in spirit of DDS
 * specification.
 * 
 * @author mcr70
 * 
 * @param <T>
 *            Type of the DataWriter. Type may be obtained from an external tool
 *            like IDL compiler, or it may be more dynamically constructed
 *            Object that is used with uDDS.
 */
public class DataWriter<T> extends Entity<T> {
    private final RTPSWriter<T> rtps_writer;
    private final HistoryCache<T> hCache;

    /**
     * Creates this DataWriter with given topic name.
     * 
     * @param topicName
     */
    DataWriter(Participant p, Class<T> type, RTPSWriter<T> writer, HistoryCache<T> hCache) {
        super(p, type, writer.getTopicName());
        this.rtps_writer = writer;
        this.hCache = hCache;
    }

    /**
     * Writes a sample to subscribed data readers.
     * 
     * @param sample
     */
    public void write(T sample) {
        LinkedList<T> ll = new LinkedList<>();
        ll.add(sample);
        write(ll);
    }

    /**
     * Writes a List of samples to subscribed data readers.
     * 
     * @param samples a List of samples
     */
    public void write(List<T> samples) {
        try {
            hCache.write(samples);
        } finally {
            notifyReaders();
        }
    }

    /**
     * Dispose a given instance.
     * 
     * @param instance
     */
    public void dispose(T instance) {
        LinkedList<T> ll = new LinkedList<>();
        ll.add(instance);
        dispose(ll);
    }

    /**
     * Dispose a List of instances.
     * 
     * @param instances
     */
    public void dispose(List<T> instances) {
        try {
            hCache.dispose(instances);
        } finally {
            notifyReaders();
        }
    }

    RTPSWriter<T> getRTPSWriter() {
        return rtps_writer;
    }

    /**
     * Gets the Guid of thie DataWriter
     * 
     * @return Guid
     */
    Guid getGuid() {
        return rtps_writer.getGuid();
    }

    /**
     * Notifies readers of the changes available.
     */
    void notifyReaders() {
        rtps_writer.notifyReaders();
    }
}
