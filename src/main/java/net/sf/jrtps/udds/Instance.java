package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.Sample;

/**
 * Instance
 * 
 * @author mcr70
 * @param <T>
 */
public class Instance <T> {
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

    /**
     * Adds a Sample to this Instance. 
     * @param aSample
     * @return null, is the size of the history grows because of addition. Otherwise,
     *       addition caused a drop of the oldest Sample, which will be returned.  
     */
    Sample<T> addSample(Sample<T> aSample) {
        if (maxSamplesPerInstance != -1 && 
                history.size() >= maxSamplesPerInstance) {
            throw new OutOfResources("max_samples_per_instance=" + maxSamplesPerInstance);
        }

        synchronized (history) {
            history.addFirst(aSample);

            if (history.size() > maxSize) {
                return history.removeLast(); // Discard oldest sample
            }            
        }

        return null;
    }

    /**
     * Removes a Sample from history if this Instance.
     * @param sample Sample to remove
     */
    void removeSample(Sample<T> sample) {
        synchronized (history) {
            history.remove(sample);
        }
    }
    
    
    /**
     * Gets the history of this instance. First element in the List returned represents
     * the latest Sample. Note, that returned List represents history at the time of
     * calling this method.
     * 
     * @return History of this Instance.
     */
    public List<Sample<T>> getHistory() {
        LinkedList<Sample<T>> ll = new LinkedList<>();
        synchronized (history) {
            ll.addAll(history); // TODO: should we create new List here or not
        }
       
        return ll; 
    }
    
    
    Sample<T> getLatest() {
        return history.getFirst();
    }
}
