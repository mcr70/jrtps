package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.QualityOfService.PolicyListener;
import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosHistory.Kind;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterProxy;
import net.sf.jrtps.util.Watchdog;
import net.sf.jrtps.util.Watchdog.Listener;
import net.sf.jrtps.util.Watchdog.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance represents Samples with same distinct ID. Instance has a history, which can be obtained 
 * from this class. The size of the history is configurable. Default size is 1, which means that only 
 * a latest Sample is preserved in history. 
 * 
 * @author mcr70
 * @param <T>
 */
public class Instance <T> {
    private static final Logger logger = LoggerFactory.getLogger(Instance.class);
    
    private final KeyHash key;
    private final LinkedList<Sample<T>> history = new LinkedList<>();
    private final int maxSamplesPerInstance;
    private final int maxSamples;
    
    private final QosHistory.Kind historyKind;
    private final int historyDepth;

    private final Watchdog watchdog;
    private long minimum_separation;
    private long nextTimeBasedFilterTime = System.currentTimeMillis();
    private Task tbfTask;
    private Task deadLineMonitorTask;    
    private WriterProxy owner;
    private int ownerStrength;

    Instance(KeyHash key, QualityOfService qos, Watchdog watchdog) {
        this.key = key;
        this.maxSamplesPerInstance = qos.getResourceLimits().getMaxSamplesPerInstance();
        this.maxSamples = qos.getResourceLimits().getMaxSamples();
        this.watchdog = watchdog;
        this.minimum_separation = qos.getTimeBasedFilter().getMinimumSeparation().asMillis();
        this.historyKind = qos.getHistory().getKind();

        if (historyKind == Kind.KEEP_ALL) {
            this.historyDepth = Integer.MAX_VALUE;
        }
        else {
            this.historyDepth = qos.getHistory().getDepth();    
        }
        
        qos.addPolicyListener(new PolicyListener() {
			@Override
			public void policyChanged(QosPolicy policy) {
				if (policy instanceof QosTimeBasedFilter) {
					minimum_separation = ((QosTimeBasedFilter)policy).getMinimumSeparation().asMillis();
				}
			}
		});
    }

    /**
     * Apply time based filtering. 
     * @param aSample
     * @return true, if time based filter was applied, and sample was dropped.
     */
    boolean applyTimeBasedFilter(final UDDSHistoryCache<T, ? extends DiscoveredData> hc, 
            final Sample<T> aSample) {
        // Check for time based filter. minimum_separation of 0
        // disables time based filter

        if (minimum_separation > 0 && System.currentTimeMillis() < nextTimeBasedFilterTime) {
            // If minimum_separation has not elapsed, add a watchdog task that
            // will add this latest sample in question to history cache, if
            // minimum_separation * 2 time has elapsed.
            // This provides "long" time as specified in DDS specification.
            if (tbfTask != null) {
                tbfTask.cancel(); // Cancel previous sample
            }

            // Add new 'latest' candidate to be added
            tbfTask = watchdog.addTask(2 * minimum_separation, new Listener() {
                @Override
                public void triggerTimeMissed() {
                    hc.addSample(aSample);
                }
            });

            return true;
        }

        if (tbfTask != null) {
            tbfTask.cancel(); // We are adding a sample, cancel waiting 'latest' sample
        }

        // Setup next time based filter time.
        nextTimeBasedFilterTime = System.currentTimeMillis() + minimum_separation;

        return false;
    }

    /**
     * Adds a Sample to this Instance. 
     * @param aSample
     * @return oldest sample, if history size was exceeded  
     */
    Sample<T> addSample(final Sample<T> aSample, boolean mayExceedMaxSamples) {
        if (historyKind == Kind.KEEP_ALL && history.size() == maxSamplesPerInstance) {
            cancelTimeBasedFilter();
            throw new OutOfResources(OutOfResources.Kind.MAX_SAMPLES_PER_INSTANCE_EXCEEDED, 
                    maxSamplesPerInstance);
        }

        if (deadLineMonitorTask != null) {
            deadLineMonitorTask.reset(); // reset deadline monitor
        }

        synchronized (history) {
            if (history.size() == 0) {
                history.addFirst(aSample);
            }
            else {
                Sample<T> first = history.getFirst();
                if (first.getTimestamp() <= aSample.getTimestamp()) {
                    history.addFirst(aSample);
                }
            }
            
            if (history.size() > historyDepth) {                
                return history.removeLast(); // Discard oldest sample
            }
            else if (mayExceedMaxSamples) {
                cancelTimeBasedFilter();
                throw new OutOfResources(OutOfResources.Kind.MAX_SAMPLES_EXCEEDED, maxSamples);
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
     * @return Latest sample, or null if there no samples
     */
    Sample<T> getLatest() {
        if (history.size() > 0) {
            return history.getFirst();
        }

        return null;
    }

    /**
     * This method is called when Instance is being disposed.
     */
    void dispose() {
        if (deadLineMonitorTask != null) {
            deadLineMonitorTask.cancel();
        }
    }

    /**
     * Cancel time based filter
     */
    private void cancelTimeBasedFilter() {
        if (tbfTask != null) {
            tbfTask.cancel();
        }
    }

    /**
     * Tries to claim ownership of this Instance. Ownership changes if strength of the writer
     * is greater than strength of the current owner, or owners Guid.compareTo(writer.getGuid()) is
     * less than 0 
     * @param writerGuid
     * @return true if ownership changed
     */
    boolean claimOwnership(WriterProxy writer) {
        if (owner == null || !owner.isAlive() || this.ownerStrength < writer.getStrength() ||
                owner.getGuid().compareTo(writer.getGuid()) < 0) {
            this.owner = writer;
            this.ownerStrength = writer.getStrength();
            
            return true;
        }
        
        return false;
    }

    public String toString() {
        return key.toString();
    }

    /**
     * Sets the deadline monitor task of this Instance. This is set during 
     * construction of Instance, if deadline qos has been set to anything other than 
     * INFINITE.
     * @param wdTask
     */
    void setDeadlineMonitorTask(Task wdTask) {
        deadLineMonitorTask = wdTask;
    }

    /**
     * This method is called by deadline monitor tasks, when deadline has been
     * missed. Clears ownership of this instance.
     */
    void deadlineMissed() {
        owner = null;
        ownerStrength = 0;
    }
}
