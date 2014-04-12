package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.Sample;

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

    Instance(KeyHash key, int historySize) {
        this.key = key;
        this.maxSize = historySize;
    }

    /**
     * Adds a Sample to this Instance. 
     * @param aSample
     * @return null, is the size of the history grows because of addition. Otherwise,
     *       addition caused a drop of the oldest Sample, which will be returned.  
     */
    Sample<T> addSample(Sample<T> aSample) {
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


    Sample<T> getLatest() {
        return history.getFirst();
    }

    public String toString() {
        return key.toString();
    }
}
