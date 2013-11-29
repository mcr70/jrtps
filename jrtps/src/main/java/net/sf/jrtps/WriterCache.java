package net.sf.jrtps;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosResourceLimits;

/*
 * This is an experimental class. It is not used at the moment. 
 * An effort to tie QosResourceLimits, QosHistory and QosReliability together. 
 */
class WriterCache<T> {
	private final QosResourceLimits resource_limits;
	private final QosHistory history;
	private final QosReliability reliability;
	
	// Main collection to hold instances. ResourceLimits is checked against this map
	private Map<InstanceKey, Instance> instances = new LinkedHashMap<>();
	
	// This list keeps changes in linear order as they happen. It is used for
	// Transmitting changes to remote readers.
	private List<CacheChange> ccList = new LinkedList<>();
	
	private Marshaller<T> marshaller;

	WriterCache(Marshaller<T> marshaller, QualityOfService qos) {
		this.marshaller = marshaller;
		resource_limits = (QosResourceLimits) qos.getPolicy(QosResourceLimits.class);
		history = (QosHistory) qos.getPolicy(QosHistory.class);
		reliability = (QosReliability) qos.getPolicy(QosReliability.class);
	}
	
	void write(T sample) {
		InstanceKey key = new InstanceKey(marshaller.extractKey(sample));
		Instance inst = instances.get(key);
		if (inst == null) {
			if (instances.size() >= resource_limits.getMaxInstances()) {
				throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
			}
			
			inst = new Instance(key, history.getDepth());
			instances.put(key, inst);
		}
		
		inst.write(sample);
	}
	
	void dispose(T sample) {
		InstanceKey key = new InstanceKey(marshaller.extractKey(sample));
		Instance inst = instances.get(key);
		if (inst == null) {
			if (instances.size() >= resource_limits.getMaxInstances()) {
				throw new OutOfResources("max_instances=" + resource_limits.getMaxInstances());
			}

			inst = new Instance(key, history.getDepth());
			instances.put(key, inst);
		}

		inst.write(sample);
	}


	class Instance {		
		private InstanceKey key;
		private InstanceHistory history;
		
		Instance(InstanceKey key, int historySize) {
			this.key = key;
			this.history = new InstanceHistory(historySize);
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}	

		int write(T sample) {
			history.add(sample);
			return 0;
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
	
	class InstanceHistory {
		private static final long serialVersionUID = 1L;
		private LinkedList<T> history = new LinkedList<>();
		private final int maxSize;

		InstanceHistory(int maxSize) {
			this.maxSize = maxSize;	
		}
		
		void add(T sample) {
			history.add(sample);
			if (history.size() > maxSize) {
				if (reliability.getKind() == QosReliability.Kind.RELIABLE) {
					// if (transmitCache.isAcknowledged(history.first())) {
					//    history.removeFirst();
					// } else {
					//     block(reliability.getMaxBlockingTime());
					//     if (!transmitCache.isAcknowledged(history.first())) {
					//        throw new OutOfResources("history is full and it is not acknowledged by every reader");
					//     }
					// }
				}
				
				history.removeFirst();
			}
		}
	}	
}
