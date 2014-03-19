package net.sf.jrtps.udds;

import java.util.LinkedList;

import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.rtps.CacheChange;

class Instance {
    private final InstanceKey key;
    private final LinkedList<CacheChange> history = new LinkedList<>();
    private final int maxSize;
    private final int maxSamplesPerInstance;

    Instance(InstanceKey key, int historySize, int maxSamplesPerInstance) {
        this.key = key;
        this.maxSize = historySize;
        this.maxSamplesPerInstance = maxSamplesPerInstance;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    CacheChange addSample(CacheChange aChange) {
        if (maxSamplesPerInstance != -1 && 
                history.size() >= maxSamplesPerInstance) {
            throw new OutOfResources("max_samples_per_instance=" + maxSamplesPerInstance);
        }

        history.addFirst(aChange);

        if (history.size() > maxSize) {
            return history.removeFirst(); // Discard oldest sample
        }

        return null;
    }

    void removeLatest() {
        history.removeFirst();
    }
}
