package net.sf.jrtps.udds;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.rtps.CacheChange;
import net.sf.jrtps.rtps.CacheChange.Kind;
import net.sf.jrtps.rtps.ReaderCache;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterCache;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HistoryCache holds Samples of entities. For writers, it is used to keep keep
 * history of changes so that late joining readers are capable of getting the historical data.
 * <p>
 * Samples on the reader side are made available through HistoryCache.
 */
class HistoryCache<T> implements WriterCache<T>, ReaderCache<T> {
    private static final Logger log = LoggerFactory.getLogger(HistoryCache.class);
    // QoS policies affecting writer cache
    private final QosResourceLimits resource_limits;
    private final QosHistory history;
    private final Map<Integer, List<CacheChange<T>>> incomingChanges = new HashMap<>();

    private final List<SampleListener<T>> listeners = new LinkedList<>();
    
    private volatile long seqNum; // sequence number of a change

    // Main collection to hold instances. ResourceLimits is checked against this map
    private final Map<KeyHash, Instance<T>> instances = new LinkedHashMap<>();
    // An ordered set of cache changes.
    private final SortedSet<CacheChange<T>> changes = Collections.synchronizedSortedSet(new TreeSet<>(
            new Comparator<CacheChange<T>>() {
                @Override
                public int compare(CacheChange<T> o1, CacheChange<T> o2) {
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
    
    void addListener(SampleListener<T> aListener) {
        listeners.add(aListener);
    }
    
    void removeListener(SampleListener<T> aListener) {
        listeners.remove(aListener);
    }

    
    private void addSample(CacheChange.Kind kind, List<T> samples) {
        log.trace("[{}] add {} samples of kind {}", entityId, samples.size(), kind);

        for (T sample : samples) {
            CacheChange<T> newChange = new CacheChange<T>(marshaller, kind, ++seqNum, sample);
            addChange(newChange);
        }
    }

    
    private void addChange(CacheChange<T> cc) {
        // TODO: InstanceKey should take KeyHash as constructor, not byte[]
        //       Alternatively, we could use KeyHash as a key to instances Map
        KeyHash key = cc.getKey();
        Kind kind = cc.getKind();
        
        if (kind != CacheChange.Kind.DISPOSE) {
            instances.remove(key);
        }
        else {
            Instance<T> inst = instances.get(key);

            if (inst == null) {
                log.trace("[{}] Creating new instance {}", entityId, key);

                if (resource_limits.getMaxInstances() != -1 && 
                        instances.size() >= resource_limits.getMaxInstances()) {
                    throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
                }

                inst = new Instance<T>(key, history.getDepth(), resource_limits.getMaxSamplesPerInstance());
                instances.put(key, inst);
            }   

            log.trace("[{}] Creating cache change {}", entityId, seqNum + 1);

            CacheChange<T> removedSample = inst.addSample(cc);
            if (removedSample != null) {
                synchronized (changes) {
                    changes.remove(removedSample);
                }
            }
        }

        if (resource_limits.getMaxSamples() != -1 && 
                changes.size() >= resource_limits.getMaxSamples()) {
            throw new OutOfResources("max_samples=" + resource_limits.getMaxSamples());
        }

        synchronized (changes) {
            changes.add(cc);
        }
    }
    

    // ----  WriterCache implementation follows  -------------------------
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
    public LinkedList<CacheChange<T>> getChangesSince(long sequenceNumber) {
        log.trace("[{}] getChangesSince({})", entityId, sequenceNumber);

        synchronized (changes) {
            for (CacheChange<T> cc : changes) {
                if (cc.getSequenceNumber() > sequenceNumber) {
                    SortedSet<CacheChange<T>> tailSet = changes.tailSet(cc);
                    log.trace("[{}] returning {}", entityId, tailSet);

                    return new LinkedList<CacheChange<T>>(tailSet);
                }
            }
        }

        log.trace("[{}] No chances to return for seq num {}", entityId, sequenceNumber);
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
        synchronized (changes) {
            if (changes.size() > 0) {
                seqNumMin = changes.first().getSequenceNumber();
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
        synchronized (changes) {
            if (changes.size() > 0) {
                seqNumMax = changes.last().getSequenceNumber();
            }
        }

        return seqNumMax;
    }



    // ----  ReaderCache implementation follows  -------------------------
    @Override
    public void changesBegin(int id) {
        log.debug("changesBegin({})", id);
        List<CacheChange<T>> pendingSamples = new LinkedList<>();
        incomingChanges.put(id, pendingSamples);
    }

    @Override
    public T addChange(int id, Guid writerGuid, Data data, Time timestamp) {
        List<CacheChange<T>> pendingSamples = incomingChanges.get(id); 
        
        CacheChange<T> cc = null;
        try {
            cc = new CacheChange<T>(writerGuid, marshaller, ++seqNum, data, timestamp.timeMillis());
            pendingSamples.add(cc);
        } catch (IOException ioe) {
            log.warn("Failed to create CacheChange", ioe);
        }
        
        return cc.getData();
    }

    @Override
    public void changesEnd(int id) {
        log.debug("changesEnd({})", id);        

        List<CacheChange<T>> pendingSamples = incomingChanges.remove(id); 

        // Add each pending CacheChange to HistoryCache
        for (CacheChange<T> cc : pendingSamples) {
            long latestSampleTime = 0;
            if (changes.size() > 0) {
                latestSampleTime = changes.last().getTimeStamp();
            }

            if (cc.getTimeStamp() < latestSampleTime) {
                log.debug("Rejecting sample since its timestamp {} is older than latest in cache {}", 
                        cc.getTimeStamp(), latestSampleTime);
                return;
            }
            else {
                addChange(cc);
            }
        }
        
        // Notify listeners 
        List<Sample<T>> samples = convertChangesToSamples(pendingSamples);
        for (SampleListener<T> aListener : listeners) {
            aListener.onSamples(new LinkedList<>(samples)); // each Listener has its own List
        }
    }

    private List<Sample<T>> convertChangesToSamples(List<CacheChange<T>> changes) {
        List<Sample<T>> samples = new LinkedList<>();

        for (CacheChange<T> cc : changes) {
            samples.add(new Sample<T>(cc));
        }
        
        return samples;
    }
}
