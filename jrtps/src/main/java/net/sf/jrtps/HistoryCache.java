package net.sf.jrtps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosResourceLimits;

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
class HistoryCache<T> {
	private static final Logger log = LoggerFactory.getLogger(HistoryCache.class);
	// QoS policies affecting writer cache
	private final QosResourceLimits resource_limits;
	private final QosHistory history;
	private final QosReliability reliability;

	private int sampleCount = 0;
	private int instanceCount = 0; // instanceCount might be smaller than instances.size() (dispose)
	private volatile int seqNum; // sequence number of a change

	// Main collection to hold instances. ResourceLimits is checked against this map
	private final Map<InstanceKey, Instance> instances = new LinkedHashMap<>();
	// An ordered set of cache changes.  
	private final SortedSet<CacheChange> changes = 
			Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<CacheChange>() {
				@Override
				public int compare(CacheChange o1, CacheChange o2) {
					return (int) (o1.getSequenceNumber() - o2.getSequenceNumber());
				}
			}));

	private final Marshaller<T> marshaller;
	private final RTPSWriter<T> rtps_writer;


	HistoryCache(Marshaller<T> marshaller, QualityOfService qos, RTPSWriter<T> rtps_writer) {
		this.marshaller = marshaller;
		this.rtps_writer = rtps_writer;

		resource_limits = (QosResourceLimits) qos.getPolicy(QosResourceLimits.class);
		history = (QosHistory) qos.getPolicy(QosHistory.class);
		reliability = (QosReliability) qos.getPolicy(QosReliability.class);
	}

	void dispose(List<T> samples) {
		addSample(ChangeKind.DISPOSE, samples);
	}

	void unregister(List<T> samples) {
		addSample(ChangeKind.UNREGISTER, samples);
	}

	void write(List<T> samples) {
		addSample(ChangeKind.WRITE, samples);
	}



	private void addSample(ChangeKind kind, List<T> samples) {
		try {
			for (T sample : samples) {
				InstanceKey key = new InstanceKey(marshaller.extractKey(sample));
				Instance inst = instances.get(key);
				if (inst == null) {
					log.trace("[{}] Creating new instance", rtps_writer.getGuid().entityId);
					instanceCount++;
					if (instanceCount > resource_limits.getMaxInstances()) {
						instanceCount = resource_limits.getMaxInstances();
						throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
					}

					inst = new Instance(key, history.getDepth());
					instances.put(key, inst);
				}

				if (inst.history.size() >= resource_limits.getMaxSamplesPerInstance()) {
					throw new OutOfResources("max_samples_per_instance=" + resource_limits.getMaxSamplesPerInstance());
				}
				
				log.trace("[{}] Creating cache change {}", rtps_writer.getGuid().entityId, seqNum + 1);
				CacheChange aChange = new CacheChange(kind, ++seqNum, sample);
				sampleCount += inst.addSample(aChange);
				if (sampleCount > resource_limits.getMaxSamples()) {
					inst.history.removeLast();
					sampleCount = resource_limits.getMaxSamples();
					throw new OutOfResources("max_samples=" + resource_limits.getMaxSamples());
				}
				changes.add(aChange);
			}
		}
		finally {
			rtps_writer.notifyReaders();
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

		// TODO: CacheChange.sequenceNumber must be set only if it is succesfully 
		//       inserted into cache
		int addSample(CacheChange aChange) {
			int historySizeChange = 1;
			history.add(aChange);
			if (history.size() > maxSize) {
				if (reliability.getKind() == QosReliability.Kind.RELIABLE) {
					CacheChange oldestChange = history.getFirst();
					if (!rtps_writer.isAcknowledgedByAll(oldestChange.getSequenceNumber())) {
						// Block the writer and hope that readers acknowledge all the changes
						
						// TODO: during acknack, we should check if there is no need to block anymore.
						//       I.e. we should notify blocked thread.
						
						log.debug("Blocking the writer for {} ms", reliability.getMaxBlockingTime().asMillis());
						rtps_writer.getParticipant().waitFor((int) reliability.getMaxBlockingTime().asMillis());
						
						if (!rtps_writer.isAcknowledgedByAll(oldestChange.getSequenceNumber())) {
							throw new TimeOutException("Blocked writer for " + reliability.getMaxBlockingTime().asMillis() + 
									" ms, and readers have not acknowledged " + oldestChange.getSequenceNumber());
						}
					}
				}

				log.debug("[{}] Removing oldest sample from history", rtps_writer.getGuid().entityId);
				CacheChange cc = history.removeFirst(); // Discard oldest sample
				changes.remove(cc); // Removed oldest instance sample from a set of changes. 

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
		public boolean equals(Object other) {
			if (other instanceof byte[]) {
				byte[] otherArray = (byte[]) other;
				return Arrays.equals(key, otherArray);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(key);
		}
	}



	/**
	 * Gets all the changes, whose sequence number is greater than given sequence number.
	 * If there is no such changes found, an empty set is returned.
	 * 
	 * @param sequenceNumber sequence number to compare to
	 * @return a SortedSet of changes
	 */
	SortedSet<CacheChange> getChangesSince(long sequenceNumber) {
		log.debug("[{}] getChangesSince({})", rtps_writer.getGuid().entityId, sequenceNumber);

		synchronized (changes) {
			for (CacheChange cc : changes) {
				if (cc.getSequenceNumber() > sequenceNumber) {
					SortedSet<CacheChange> tailSet = changes.tailSet(cc);
					log.debug("[{}] returning {}", rtps_writer.getGuid().entityId, tailSet);
					return tailSet;
				}
			}
		}

		log.debug("[{}] No chances to return for seq num {}", rtps_writer.getGuid().entityId, sequenceNumber);
		return new TreeSet<>();
	}

	public long getSeqNumMin() {
		if (changes.size() == 0) {
			return 0;
		}
		return changes.first().getSequenceNumber();
	}
	
	public long getSeqNumMax() {
		if (changes.size() == 0) {
			return 0;
		}
		return changes.last().getSequenceNumber();
	}

	public void clear() {
		instances.clear();
		changes.clear();
	}
}

