package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.rtps.RTPSWriter;

/**
 * This class represents a strongly typed DataWriter in spirit of DDS specification.
 * 
 * @author mcr70
 * 
 * @param <T>
 *            Type of the DataWriter. Type may be obtained from an external tool
 *            like IDL compiler, or it may be more dynamically constructed
 *            Object that is used with uDDS.
 */
public class DataWriter<T> extends Entity<T> {
    private final List<ReaderListener> rListeners = new LinkedList<>();
    /**
     * RTPSWriter associated with this DataWriter
     */
    protected final RTPSWriter<T> rtps_writer;
    /**
     * HistoryCache associated with this DataWriter
     */
    protected final HistoryCache<T> hCache;

    /**
     * Constructor for DataWriter.
     * 
     * @param p Participant that created this DataWriter
     * @param type Type of this DataWriter
     * @param writer associated RTPSWriter
     * @param hCache HistoryCache for DataWriter
     */
    protected DataWriter(Participant p, Class<T> type, RTPSWriter<T> writer, HistoryCache<T> hCache) {
        super(p, type, writer.getTopicName(), writer.getGuid());
        this.rtps_writer = writer;
        this.hCache = hCache;
    }
    
    /**
     * Asserts liveliness of this DataWriter. Liveliness of writers must be asserted by a 
     * call to this method, if QosLiveliness.Kind is set to MANUAL_BY_TOPIC.
     * 
     * @see QosLiveliness
     */
    public void assertLiveliness() {
        rtps_writer.assertLiveliness();
    }
    
    /**
     * Writes a sample to subscribed data readers. By writing a Sample, an Instance is first
     * created if one did not exists before for a given Sample.
     * 
     * @param sample a Sample to write
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
            long ts = System.currentTimeMillis();
            for (T sample : samples) {
                hCache.write(sample, ts);
            }
        } finally {
            notifyReaders();
        }
    }

    /**
     * Dispose a given instance. Disposing an instance removes it from the Set of know instances
     * of this DataWriter. In addition, a Sample with ChangeKind.DISPOSE is added to List of changes
     * that will be transmitted to readers.
     * 
     * After disposing an Instance, it can be recreated by writing a Sample with same ID. In this case,
     * an Instance will be reborn, but its history consist only of the new Sample just written.
     * Following diagram illustrates this behavior:  
     * 
     * <pre>
     *     App             Instance history    changes to readers
     *  1. write(S1)       S1                  S1
     *  2. write(S2)       S1,S2               S1,S2
     *  3. dispose(S3)     --                  S1,S2,S3
     *  4. write(S4)       S4                  S1,S2,S3,S4
     * </pre>
     * 
     * 
     * @param instance an Instance to dispose
     */
    public void dispose(T instance) {
        LinkedList<T> ll = new LinkedList<>();
        ll.add(instance);
        dispose(ll);
    }

    /**
     * Dispose a List of instances.
     * 
     * @param instances a List of Instances to dispose
     */
    public void dispose(List<T> instances) {
        try {
            long ts = System.currentTimeMillis();
            for (T sample : instances) {
                hCache.dispose(sample, ts);
            }
        } finally {
            notifyReaders();
        }
    }

    /**
     * Gets a Set of instances <i>this</i> DataWriter knows.   
     * 
     * @return a Set of instances
     */
    public Set<Instance<T>> getInstances() {
        return hCache.getInstances();
    }
    
    RTPSWriter<T> getRTPSWriter() {
        return rtps_writer;
    }


    /**
     * Notifies readers of the changes available.
     */
    protected void notifyReaders() {
        rtps_writer.notifyReaders();
    }

    /**
     * Adds a ReaderListener to this DataWriter.
     * @param rl ReaderListener to add
     */
    public void addReaderListener(ReaderListener rl) {
        synchronized (rListeners) {
            rListeners.add(rl);
        }
    }
    
    /**
     * Removes a ReaderListener from this DataWriter.
     * @param rl ReaderListener to remove
     */
    public void removeReaderListener(ReaderListener rl) {
        synchronized (rListeners) {
            rListeners.remove(rl);
        }
    }

    void addMatchedReader(SubscriptionData sd) {
        rtps_writer.addMatchedReader(sd);
        synchronized (rListeners) {
            for (ReaderListener rl : rListeners) {
                rl.readerMatched(sd);
            }
        }
    }

    void removeMatchedReader(SubscriptionData sd) {
        rtps_writer.removeMatchedReader(sd);
    }

    void inconsistentQoS(SubscriptionData sd) {
        synchronized (rListeners) {
            for (ReaderListener rl : rListeners) {
                rl.inconsistentQoS(sd);
            }
        }
    }
}
