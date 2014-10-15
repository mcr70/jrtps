package net.sf.jrtps.udds;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.rtps.ChangeKind;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.util.Watchdog;
import net.sf.jrtps.util.Watchdog.Listener;
import net.sf.jrtps.util.Watchdog.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HistoryCache holds Samples of entities. For writers, it is used to keep keep
 * history of changes so that late joining readers are capable of getting the historical data.
 * <p>
 * Samples on the reader side are made available through HistoryCache.
 * 
 * @param T type of the samples managed by this HistoryCache
 * @param ENTITY_DATA type of the communication listeners attached to this history cache.
 */
class UDDSHistoryCache<T, ENTITY_DATA extends DiscoveredData> implements HistoryCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(UDDSHistoryCache.class);
    // QoS policies affecting writer cache
    private final QosResourceLimits resource_limits;
    private final QosHistory history;

    protected final List<SampleListener<T>> listeners = new LinkedList<>();    
    private volatile CoherentSet coherentSet; // Current CoherentSet, used by writer

    // Main collection to hold instances. ResourceLimits is checked against this map
    private final Map<KeyHash, Instance<T>> instances = new LinkedHashMap<>();

    private long deadLinePeriod = -1; // -1 represents INFINITE

    // For readers time based filter:
    private final long minimumSeparationMillis;

    // An ordered set of cache changes.
    protected final SortedSet<Sample<T>> samples = Collections.synchronizedSortedSet(new TreeSet<>(
            new Comparator<Sample<T>>() {
                @Override
                public int compare(Sample<T> o1, Sample<T> o2) {
                    return (int) (o1.getSequenceNumber() - o2.getSequenceNumber());
                }
            }));

    protected final Marshaller<T> marshaller;
    protected volatile long seqNum; // sequence number of a Sample
    protected final EntityId entityId;

    protected final Watchdog watchdog;
    private List<CommunicationListener<ENTITY_DATA>> communicationListeners;
    private final QualityOfService qos;


    UDDSHistoryCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog, boolean isReaderCache) {
        this.entityId = eId;
        this.marshaller = marshaller;
        this.qos = qos;
        this.watchdog = watchdog;

        Duration period = qos.getDeadline().getPeriod();

        if (!Duration.INFINITE.equals(period)) { 
            this.deadLinePeriod = period.asMillis();

            logger.debug("deadline period was set to {}", deadLinePeriod);
        }

        resource_limits = qos.getResourceLimits();
        history = qos.getHistory();

        if (isReaderCache) {
            minimumSeparationMillis = qos.getTimeBasedFilter().getMinimumSeparation().asMillis();
        }
        else {
            minimumSeparationMillis = 0;
        }
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

    protected void addSample(Sample<T> sample) {
        logger.trace("addSample({})", sample);
        KeyHash key = sample.getKey();
        ChangeKind kind = sample.getKind();

        sample.setCoherentSet(coherentSet); // Set the CoherentSet attribute, if it exists

        Instance<T> inst = null;
        try {
            inst = getOrCreateInstance(key);
            Sample<T> latest = inst.getLatest();
            if (latest != null && latest.getTimestamp() > sample.getTimestamp()) {
                logger.debug("Rejecting sample, since its timestamp {} is less than instances latest timestamp {}", 
                        sample.getTimestamp(), latest.getTimestamp());
                return;
            }

            if (inst.applyTimeBasedFilter(this, sample)) { // Check, if TIME_BASED_FILTER applies
                return;
            }

            if (kind == ChangeKind.DISPOSE) {
                Instance<T> removedInstance = instances.remove(key);
                if (removedInstance != null) {
                    removedInstance.dispose(); // cancels deadline monitor
                }
            }
            else {
                logger.trace("[{}] Creating sample {}", entityId, seqNum + 1);
                
                Sample<T> removedSample =  
                        inst.addSample(sample, samples.size() == resource_limits.getMaxSamples());

                if (removedSample != null) {
                    synchronized (samples) {
                        samples.remove(removedSample);
                    }
                }
            }

            synchronized (samples) {
                samples.add(sample);
            }
        }
        catch(OutOfResources oor) {
            logger.debug("Got OutOfResources: {}", oor.getMessage());
            throw oor;
        }
    }


    protected Instance<T> getOrCreateInstance(final KeyHash key) {
        Instance<T> inst = instances.get(key);
        if (inst == null) {

            logger.trace("[{}] Creating new instance {}", entityId, key);

            if (resource_limits.getMaxInstances() != -1 && 
                    instances.size() == resource_limits.getMaxInstances()) {
                throw new OutOfResources(OutOfResources.Kind.MAX_INSTANCES_EXCEEDED, 
                        resource_limits.getMaxInstances());
            }

            Task wdTask = null;
            if (deadLinePeriod != -1) {
                wdTask = watchdog.addTask(deadLinePeriod, new Listener() {
                    @Override
                    public void triggerTimeMissed() {
                        logger.debug("deadline missed for {}", key);
                        for (CommunicationListener<?> cl : communicationListeners) {
                            cl.deadlineMissed(key);
                        }
                    }
                });
            }

            inst = new Instance<T>(key, qos, watchdog, wdTask);
            instances.put(key, inst);
        }   

        return inst;
    }




    // --- experimental code follows. These are paired with the ones in DataReader  ----------------
    @Override
    public Set<Instance<T>> getInstances() {
        Collection<Instance<T>> values = instances.values();
        return new HashSet<>(values);
    }

    @Override
    public Instance<T> getInstance(KeyHash key) {
        return instances.get(key);
    }

    void clear(Sample<T> aSample) {
        LinkedList<Sample<T>> samples = new LinkedList<>();
        samples.add(aSample);
        clear(samples);
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


    void setCommunicationListeners(List<CommunicationListener<ENTITY_DATA>> communicationListeners) {
        this.communicationListeners = communicationListeners;        
    }
}
