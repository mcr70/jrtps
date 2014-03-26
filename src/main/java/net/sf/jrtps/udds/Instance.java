package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.Sample;

class Instance <T> {
    private final KeyHash key;
    private final LinkedList<Sample<T>> history = new LinkedList<>();
    private final int maxSize;
    private final int maxSamplesPerInstance;

    Instance(KeyHash key, int historySize, int maxSamplesPerInstance) {
        this.key = key;
        this.maxSize = historySize;
        this.maxSamplesPerInstance = maxSamplesPerInstance;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    Sample<T> addSample(Sample<T> aChange) {
        if (maxSamplesPerInstance != -1 && 
                history.size() >= maxSamplesPerInstance) {
            throw new OutOfResources("max_samples_per_instance=" + maxSamplesPerInstance);
        }

        history.addFirst(aChange);

        if (history.size() > maxSize) {
            return history.removeLast(); // Discard oldest sample
        }

        return null;
    }

    /**
     * Gets the history of this instance.
     * 
     * @return
     */
    List<Sample<T>> getHistory() {
        // TODO: should we create new List here or not
       return new LinkedList<>(history); 
    }
    
    Sample<T> getLatest() {
        return history.getFirst();
    }
    
    void removeLatest() {
        history.removeFirst();
    }
}
