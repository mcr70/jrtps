package net.sf.jrtps.udds;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosDestinationOrder.Kind;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.rtps.ChangeKind;
import net.sf.jrtps.rtps.ReaderCache;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterCache;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.types.Time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HistoryCache holds Samples of entities. For writers, it is used to keep keep
 * history of changes so that late joining readers are capable of getting the historical data.
 * <p>
 * Samples on the reader side are made available through HistoryCache.
 */
class UDDSHistoryCache<T> implements HistoryCache<T>, WriterCache<T>, ReaderCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(UDDSHistoryCache.class);
    // QoS policies affecting writer cache
    private final QosResourceLimits resource_limits;
    private final QosHistory history;
    private final Map<Integer, List<Sample<T>>> incomingSamples = new HashMap<>();

    private final List<SampleListener<T>> listeners = new LinkedList<>();

    private volatile long seqNum; // sequence number of a Sample
    private volatile CoherentSet coherentSet; // Current CoherentSet, used by writer
    private Map<Guid, List<Sample<T>>> coherentSets = new HashMap<>(); // Used by reader

    // Main collection to hold instances. ResourceLimits is checked against this map
    private final Map<KeyHash, Instance<T>> instances = new LinkedHashMap<>();

    // An ordered set of cache changes.
    private final SortedSet<Sample<T>> samples = Collections.synchronizedSortedSet(new TreeSet<>(
            new Comparator<Sample<T>>() {
                @Override
                public int compare(Sample<T> o1, Sample<T> o2) {
                    return (int) (o1.getSequenceNumber() - o2.getSequenceNumber());
                }
            }));

    private final Marshaller<T> marshaller;
    private final EntityId entityId;
    private final Kind destinationOrderKind;


    UDDSHistoryCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos) {
        this.entityId = eId;
        this.marshaller = marshaller;

        resource_limits = qos.getResourceLimits();
        history = qos.getHistory();
        destinationOrderKind = qos.getDestinationOrder().getKind();
    }

    /**
     * Dispose a sample.
     * @param sample
     * @param timestamp
     */
    @Override
    public void dispose(T sample, long timestamp) {
        addSample(new Sample<T>(null, marshaller, ++seqNum, timestamp, ChangeKind.DISPOSE, sample));
    }

    /**
     * Unregisters an instance.
     * @param sample
     * @param timestamp
     */
    @Override
    public void unregister(T sample, long timestamp) {
        addSample(new Sample<T>(null, marshaller, ++seqNum, timestamp, ChangeKind.UNREGISTER, sample));
    }

    /**
     * Writes a sample.
     * @param sample
     * @param timestamp
     */
    @Override
    public void write(T sample, long timestamp) {
        addSample(new Sample<T>(null, marshaller, ++seqNum, timestamp, ChangeKind.WRITE, sample));
    }

    /**
     * Registers an instance
     * @param sample
     * @param timestamp
     * @return an Instance
     */
    @Override
    public Instance<T> register(T sample, long timestamp) {
        Sample<T> dummySample = new Sample<T>(null, marshaller, ++seqNum, System.currentTimeMillis(), null, sample);
        return getOrCreateInstance(dummySample.getKey());
    }


    void addListener(SampleListener<T> aListener) {
        listeners.add(aListener);
    }

    void removeListener(SampleListener<T> aListener) {
        listeners.remove(aListener);
    }

    private void addSample(Sample<T> sample) {
        logger.trace("addSample({})", sample);
        KeyHash key = sample.getKey();
        ChangeKind kind = sample.getKind();

        sample.setCoherentSet(coherentSet); // Set the CoherentSet attribute, if it exists

        if (kind == ChangeKind.DISPOSE) {
            instances.remove(key);
        }
        else {
            Instance<T> inst = getOrCreateInstance(key);

            logger.trace("[{}] Creating sample {}", entityId, seqNum + 1);

            Sample<T> removedSample = inst.addSample(sample);
            if (removedSample != null) {
                synchronized (samples) {
                    samples.remove(removedSample);
                }
            }
        }

        if (resource_limits.getMaxSamples() != -1 && 
                samples.size() >= resource_limits.getMaxSamples()) {
            throw new OutOfResources("max_samples=" + resource_limits.getMaxSamples());
        }

        synchronized (samples) {
            samples.add(sample);
        }
    }


    private Instance<T> getOrCreateInstance(KeyHash key) {
        Instance<T> inst = instances.get(key);
        if (inst == null) {

            logger.trace("[{}] Creating new instance {}", entityId, key);

            if (resource_limits.getMaxInstances() != -1 && 
                    instances.size() > resource_limits.getMaxInstances()) {
                throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
            }

            inst = new Instance<T>(key, history.getDepth());
            instances.put(key, inst);
        }   

        return inst;
    }

    // ----  WriterCache implementation follows  -------------------------
    /**
     * Gets all the Samples, whose sequence number is greater than given
     * sequence number. If there is no such samples found, an empty set is
     * returned.
     * 
     * @param sequenceNumber sequence number to compare to
     * @return a SortedSet of Samples
     */
    @Override
    public LinkedList<Sample<T>> getSamplesSince(long sequenceNumber) {
        logger.trace("[{}] getChangesSince({})", entityId, sequenceNumber);

        synchronized (samples) {
            for (Sample<T> cc : samples) {
                if (cc.getSequenceNumber() > sequenceNumber) {
                    SortedSet<Sample<T>> tailSet = samples.tailSet(cc);
                    logger.trace("[{}] returning {}", entityId, tailSet);

                    return new LinkedList<Sample<T>>(tailSet);
                }
            }
        }

        logger.trace("[{}] No chances to return for seq num {}", entityId, sequenceNumber);
        return new LinkedList<>();
    }

    /**
     * Gets the smallest sequence number this HistoryCache has.
     * 
     * @return seqNumMin
     */
    @Override
    public long getSeqNumMin() {
        long seqNumMin = 0;
        synchronized (samples) {
            if (samples.size() > 0) {
                seqNumMin = samples.first().getSequenceNumber();
            }
        }

        return seqNumMin;
    }

    /**
     * Gets the biggest sequence number this HistoryCache has.
     * 
     * @return seqNumMax
     */
    @Override
    public long getSeqNumMax() {
        long seqNumMax = 0;

        synchronized (samples) {
            if (samples.size() > 0) {
                seqNumMax = samples.last().getSequenceNumber();
            }
        }

        return seqNumMax;
    }



    // ----  ReaderCache implementation follows  -------------------------
    @Override
    public void changesBegin(int id) {
        logger.trace("changesBegin({})", id);
        List<Sample<T>> pendingSamples = new LinkedList<>();
        incomingSamples.put(id, pendingSamples);
    }

    @Override
    public void addChange(int id, Guid writerGuid, Data data, Time timestamp) {

        long ts = 0;
        if (destinationOrderKind == Kind.BY_RECEPTION_TIMESTAMP || timestamp == null) {
            ts = System.currentTimeMillis();
        }
        else {
            ts = timestamp.timeMillis(); 
        }

        List<Sample<T>> coherentSet = getCoherentSet(writerGuid); // Get current CoherentSet for writer
        List<Sample<T>> pendingSamples = incomingSamples.get(id); 

        Sample<T> sample = new Sample<T>(writerGuid, marshaller, ++seqNum, ts, data);
        CoherentSet cs = sample.getCoherentSet();

        // Check, if we need to add existing CoherentSet into pendingSamples
        if (coherentSet.size() > 0) { // If no samples in cs, no need to add to pending samples 
            if (cs == null || 
                    cs.getStartSeqNum().getAsLong() == SequenceNumber.SEQUENCENUMBER_UNKNOWN.getAsLong() ||
                    cs.getStartSeqNum().getAsLong() != coherentSet.get(0).getCoherentSet().getStartSeqNum().getAsLong()) {
                // End of CoherentSet is detected, if CS attribute is missing, or it is SEQNUM_UNKNOWN,
                // or its startSeqNum is different
                pendingSamples.addAll(coherentSet);
                coherentSet.clear();
            }
        }

        if (data.dataFlag()) { // Add only Samples with contain Data
            if (cs != null) { // If we have a CS attribute, add it to coherentSet
                coherentSet.add(sample);
            }
            else {
                pendingSamples.add(sample);
            }
        }
        else {
            logger.debug("Skipping sample #{} from being delivered to reader, since it does not contain Data", data.getWriterSequenceNumber());
        }
    }

    private List<Sample<T>> getCoherentSet(Guid writerGuid) {
        List<Sample<T>> list = coherentSets.get(writerGuid);
        if (list == null) {
            list = new LinkedList<>();
            coherentSets.put(writerGuid, list);
        }

        return list;
    }

    @Override
    public void changesEnd(int id) {
        logger.trace("changesEnd({})", id);        

        List<Sample<T>> pendingSamples = incomingSamples.remove(id); 

        // Add each pending Sample to HistoryCache
        for (Sample<T> cc : pendingSamples) {
            long latestSampleTime = 0;
            if (samples.size() > 0) {
                latestSampleTime = samples.last().getTimestamp();
            }

            if (cc.getTimestamp() < latestSampleTime) {
                logger.debug("Rejecting sample since its timestamp {} is older than latest in cache {}", 
                        cc.getTimestamp(), latestSampleTime);
                continue;
            }
            else {
                addSample(cc);
            }
        }

        // Notify listeners 
        for (SampleListener<T> aListener : listeners) {
            aListener.onSamples(new LinkedList<>(pendingSamples)); // each Listener has its own List
        }
    }


    // --- experimental code follows. These are paired with the ones in DataReader  ----------------
    @Override
    public Set<Instance<T>> getInstances() {
        Collection<Instance<T>> values = instances.values();
        return new HashSet<>(values);
    }

    Instance<T> getInstance(KeyHash key) {
        return instances.get(key);
    }

    void clear(List<Sample<T>> samplesToClear) {
        for (Sample<T> s : samplesToClear) {
            Instance<T> inst = instances.get(s.getKey());
            inst.removeSample(s);

            synchronized (samples) {
                samples.remove(s);    
            }
        }
    }

    @Override
    public void coherentChangesBegin() {
        coherentSet = new CoherentSet(new SequenceNumber(seqNum + 1));
        logger.debug("coherentChangesBegin({})", seqNum + 1);
    }

    @Override
    public void coherentChangesEnd() {
        if (coherentSet != null) {
            logger.debug("coherentChangesEnd({})", coherentSet.getStartSeqNum().getAsLong());
        }
        coherentSet = null;
        addSample(new Sample<T>(++seqNum)); // Add a Sample denoting end of CoherentSet
    }
}
