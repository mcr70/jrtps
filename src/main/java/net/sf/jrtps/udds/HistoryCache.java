package net.sf.jrtps.udds;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.rtps.CacheChange;
import net.sf.jrtps.rtps.WriterCache;
import net.sf.jrtps.types.EntityId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HistoryCache holds Samples of entities. For writers, it is used to keep keep
 * history of changes so that late joining readers are capable of getting the historical data.
 * <p>
 * Samples on the reader side are made available through HistoryCache.
 */
class HistoryCache<T> implements WriterCache {
    private static final Logger log = LoggerFactory.getLogger(HistoryCache.class);
    // QoS policies affecting writer cache
    private final QosResourceLimits resource_limits;
    private final QosHistory history;
    
    private int sampleCount = 0;
    private int instanceCount = 0; // instanceCount might be smaller than
                                   // instances.size() (dispose)
    private volatile int seqNum; // sequence number of a change

    // Main collection to hold instances. ResourceLimits is checked against this
    // map
    private final Map<InstanceKey, Instance> instances = new LinkedHashMap<>();
    // An ordered set of cache changes.
    private final SortedSet<CacheChange> changes = Collections.synchronizedSortedSet(new TreeSet<>(
            new Comparator<CacheChange>() {
                @Override
                public int compare(CacheChange o1, CacheChange o2) {
                    return (int) (o1.getSequenceNumber() - o2.getSequenceNumber());
                }
            }));

    private final Marshaller<T> marshaller;
    private final EntityId entityId;

    HistoryCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos) {
        this.entityId = eId;
        this.marshaller = marshaller;

        resource_limits = qos.getResourceLimits();
        history = qos.getHistory();
        qos.getReliability();
    }

    public void dispose(List<T> samples) {
        addSample(CacheChange.Kind.DISPOSE, samples);
    }

    public void unregister(List<T> samples) {
        addSample(CacheChange.Kind.UNREGISTER, samples);
    }

    public void write(List<T> samples) {
        addSample(CacheChange.Kind.WRITE, samples);
    }

    private void addSample(CacheChange.Kind kind, List<T> samples) {
        log.trace("[{}] add {} samples of kind {}", entityId, samples.size(), kind);

        for (T sample : samples) {
            InstanceKey key = new InstanceKey(marshaller.extractKey(sample));
            Instance inst = instances.get(key);
            if (inst == null) {
                log.trace("[{}] Creating new instance {}", entityId, key);
                instanceCount++;
                if (resource_limits.getMaxInstances() != -1 && 
                		instanceCount > resource_limits.getMaxInstances()) {
                    instanceCount = resource_limits.getMaxInstances();
                    throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
                }

                inst = new Instance(key, history.getDepth(), resource_limits.getMaxSamplesPerInstance());
                instances.put(key, inst);
            }


            log.trace("[{}] Creating cache change {}", entityId, seqNum + 1);
            CacheChange newChange = new CacheChange(marshaller, kind, ++seqNum, sample);
            CacheChange removedSample = inst.addSample(newChange);
            if (removedSample != null) {
                sampleCount++;
            }
            
            if (resource_limits.getMaxSamples() != -1 && 
            		sampleCount > resource_limits.getMaxSamples()) {
                inst.removeLatest();
                sampleCount = resource_limits.getMaxSamples();
                throw new OutOfResources("max_samples=" + resource_limits.getMaxSamples());
            }
            
            changes.add(newChange);
        }
    }


    /**
     * Gets all the changes, whose sequence number is greater than given
     * sequence number. If there is no such changes found, an empty set is
     * returned.
     * 
     * @param sequenceNumber
     *            sequence number to compare to
     * @return a SortedSet of changes
     */
    @Override
    public SortedSet<CacheChange> getChangesSince(long sequenceNumber) {
        log.trace("[{}] getChangesSince({})", entityId, sequenceNumber);

        synchronized (changes) {
            for (CacheChange cc : changes) {
                if (cc.getSequenceNumber() > sequenceNumber) {
                    SortedSet<CacheChange> tailSet = changes.tailSet(cc);
                    log.trace("[{}] returning {}", entityId, tailSet);
                    return tailSet;
                }
            }
        }

        log.trace("[{}] No chances to return for seq num {}", entityId, sequenceNumber);
        return new TreeSet<>();
    }

    /**
     * Gets the smallest sequence number this HistoryCache has.
     * 
     * @return seqNumMin
     */
    @Override
    public long getSeqNumMin() {
        if (changes.size() == 0) {
            return 0;
        }
        return changes.first().getSequenceNumber();
    }

    /**
     * Gets the biggest sequence number this HistoryCache has.
     * 
     * @return seqNumMax
     */
    @Override
    public long getSeqNumMax() {
        if (changes.size() == 0) {
            return 0;
        }
        return changes.last().getSequenceNumber();
    }

    /**
     * Clears all the references to data in this HistoryCache
     */
    void clear() {
        instances.clear();
        changes.clear();
    }
}
