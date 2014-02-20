package net.sf.jrtps.udds;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.TimeOutException;
import net.sf.jrtps.WriterCache;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.rtps.CacheChange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is an experimental class. It is not used at the moment. 
 * An effort to tie QosResourceLimits, QosHistory and QosReliability together. 
 * 
 * This class may be called from a DDS writer, or from RTPS writer. When called from
 * RTPS layer, RTPSWriter requests changes to be sent to remote readers. When called 
 * from DDS layer, a DDS Writer is adding new changes to history cache.
 */
class HistoryCache<T> implements WriterCache {
    private static final Logger log = LoggerFactory.getLogger(HistoryCache.class);
    // QoS policies affecting writer cache
    private final QosResourceLimits resource_limits;
    private final QosHistory history;
    private final QosReliability reliability;

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
    private DataWriter<T> writer;

    HistoryCache(Marshaller<T> marshaller, QualityOfService qos) {
        this.marshaller = marshaller;

        resource_limits = qos.getResourceLimits();
        history = qos.getHistory();
        reliability = qos.getReliability();
    }

    void setDataWriter(DataWriter<T> dw) {
        this.writer = dw;
        log.debug("Created HistoryCache for {}: {}, {}, {}", new Object[] { dw.getGuid().getEntityId(), reliability,
                history, resource_limits });
    }

    void dispose(List<T> samples) {
        addSample(CacheChange.Kind.DISPOSE, samples);
    }

    void unregister(List<T> samples) {
        addSample(CacheChange.Kind.UNREGISTER, samples);
    }

    void write(List<T> samples) {
        addSample(CacheChange.Kind.WRITE, samples);
    }

    private void addSample(CacheChange.Kind kind, List<T> samples) {
        log.trace("[{}] add {} samples of kind {}", writer.getGuid().getEntityId(), samples.size(), kind);

        for (T sample : samples) {
            InstanceKey key = new InstanceKey(marshaller.extractKey(sample));
            Instance inst = instances.get(key);
            if (inst == null) {
                log.trace("[{}] Creating new instance {}", writer.getGuid().getEntityId(), key);
                instanceCount++;
                if (resource_limits.getMaxInstances() != -1 && 
                		instanceCount > resource_limits.getMaxInstances()) {
                    instanceCount = resource_limits.getMaxInstances();
                    throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
                }

                inst = new Instance(key, history.getDepth());
                instances.put(key, inst);
            }

            if (resource_limits.getMaxSamplesPerInstance() != -1 && 
            		inst.history.size() >= resource_limits.getMaxSamplesPerInstance()) {
                throw new OutOfResources("max_samples_per_instance=" + resource_limits.getMaxSamplesPerInstance());
            }

            log.trace("[{}] Creating cache change {}", writer.getGuid().getEntityId(), seqNum + 1);
            CacheChange aChange = new CacheChange(marshaller, kind, ++seqNum, sample);
            sampleCount += inst.addSample(aChange);
            if (resource_limits.getMaxSamples() != -1 && 
            		sampleCount > resource_limits.getMaxSamples()) {
                inst.history.removeLast();
                sampleCount = resource_limits.getMaxSamples();
                throw new OutOfResources("max_samples=" + resource_limits.getMaxSamples());
            }
            changes.add(aChange);
        }
    }

    class Instance {
        private final InstanceKey key;
        private final LinkedList<CacheChange> history = new LinkedList<>();
        private final int maxSize;

        Instance(InstanceKey key, int historySize) {
            this.key = key;
            this.maxSize = historySize;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        // TODO: CacheChange.sequenceNumber must be set only if it is
        // succesfully
        // inserted into cache
        int addSample(CacheChange aChange) {
            log.trace("[{}] Adding sample {}", writer.getGuid().getEntityId(), aChange.getSequenceNumber());
            int historySizeChange = 1;
            history.add(aChange);
            if (history.size() > maxSize) {
                if (reliability.getKind() == QosReliability.Kind.RELIABLE) {
                    CacheChange oldestChange = history.getFirst();
                    if (reliability.getMaxBlockingTime().asMillis() > 0
                            && !writer.getRTPSWriter().isAcknowledgedByAll(oldestChange.getSequenceNumber())) {
                        // Block the writer and hope that readers acknowledge
                        // all the changes

                        // TODO: during acknack, we should check if there is no
                        // need to block anymore.
                        // I.e. we should notify blocked thread.

                        log.trace("[{}] Blocking the writer for {} ms", writer.getGuid().getEntityId(), reliability
                                .getMaxBlockingTime().asMillis());
                        writer.getParticipant().waitFor(reliability.getMaxBlockingTime().asMillis());

                        if (!writer.getRTPSWriter().isAcknowledgedByAll(oldestChange.getSequenceNumber())) {
                            throw new TimeOutException("Blocked writer for "
                                    + reliability.getMaxBlockingTime().asMillis()
                                    + " ms, and readers have not acknowledged " + oldestChange.getSequenceNumber());
                        }
                    }
                }

                log.trace("[{}] Removing oldest sample from history", writer.getGuid().getEntityId());
                CacheChange cc = history.removeFirst(); // Discard oldest sample
                changes.remove(cc); // Removed oldest instance sample from a set
                                    // of changes.

                historySizeChange = 0;
            }

            return historySizeChange;
        }
    }

    class InstanceKey {
        private byte[] key;

        InstanceKey(byte[] key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof HistoryCache<?>.InstanceKey) {
                InstanceKey other = (InstanceKey) o;

                return Arrays.equals(key, other.key);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(key);
        }

        public String toString() {
            return "Key: " + Arrays.toString(key);
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
        log.trace("[{}] getChangesSince({})", writer.getGuid().getEntityId(), sequenceNumber);

        synchronized (changes) {
            for (CacheChange cc : changes) {
                if (cc.getSequenceNumber() > sequenceNumber) {
                    SortedSet<CacheChange> tailSet = changes.tailSet(cc);
                    log.trace("[{}] returning {}", writer.getGuid().getEntityId(), tailSet);
                    return tailSet;
                }
            }
        }

        log.trace("[{}] No chances to return for seq num {}", writer.getGuid().getEntityId(), sequenceNumber);
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
