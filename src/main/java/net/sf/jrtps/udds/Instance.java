package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.util.Watchdog.Task;

/**
 * Instance represents Samples with same distinct ID. Instance has a history, which can be obtained 
 * from this class. The size of the history is configurable. Default size is 1, which means that only 
 * a latest Sample is preserved in history. 
 * 
 * @author mcr70
 * @param <T>
 */
public class Instance <T> {
    private final KeyHash key;
    private final LinkedList<Sample<T>> history = new LinkedList<>();
    private final int maxSize;
    private Task deadLineMonitorTask;

    Instance(KeyHash key, int historySize, Task wdTask) {
        this.key = key;
        this.maxSize = historySize;
        this.deadLineMonitorTask = wdTask;
    }

    /**
     * Adds a Sample to this Instance. 
     * @param aSample
     * @return null, is the size of the history grows because of addition. Otherwise,
     *       addition caused a drop of the oldest Sample, which will be returned.  
     */
    Sample<T> addSample(Sample<T> aSample) {
        if (deadLineMonitorTask != null) {
            deadLineMonitorTask.reset(); // reset deadline monitor
        }
        
        synchronized (history) {
            if (history.size() == 0) {
                history.addFirst(aSample);
            }
            else {
                Sample<T> first = history.getFirst();
                if (first.getTimestamp() < aSample.getTimestamp()) {
                    history.addFirst(aSample);
                }
            }

            if (history.size() > maxSize) {
                return history.removeLast(); // Discard oldest sample
            }
        }

        return null;
    }

    /**
     * Removes a Sample from history of this Instance.
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

    /** 
     * Gets the key of this instance.
     * @return a Key that represents the key of this instance
     */
    public KeyHash getKey() {
        return key;
    }
    
    /**
     * Get the latest sample
     * @return Latest sample.
     */
    Sample<T> getLatest() {
        return history.getFirst();
    }
    
    /**
     * This method is called when Instance is being disposed.
     */
    void dispose() {
        if (deadLineMonitorTask != null) {
            deadLineMonitorTask.cancel();
        }
    }

    public String toString() {
        return key.toString();
    }
}
