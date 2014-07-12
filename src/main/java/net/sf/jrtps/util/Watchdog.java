package net.sf.jrtps.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Watchdog class utilizes ScheduledExecutorService to provide 
 * applications watchdog functionality. Multiple Tasks may be
 * given to be watched. 
 * 
 * @author mcr70
 */
public class Watchdog {
    private ScheduledExecutorService ses;

    /**
     * Create Watchdog with given ScheduledExecutorService.
     * @param ses
     */
    Watchdog(ScheduledExecutorService ses) {
        this.ses = ses;
    }
    
    /**
     * Adds a new Watchdog Task to executor. 
     * 
     * @param triggerTime watchdog trigger time in milliseconds. 
     * @param listener A Listener, that will be called if application does
     *        not call Task.reset() before trigger time has elapsed.
     * @return Task
     */
    Task addTask(long triggerTime, final Listener listener) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.triggerTimeMissed();
            }
        };
        
        // Create Task and start watchdog
        ScheduledFuture<?> scheduledFuture = ses.schedule(r, triggerTime, TimeUnit.MILLISECONDS);
        Task task = new Task(scheduledFuture, triggerTime, r);
        
        return task;
    }
    
    
    /**
     * Watchdog Task.
     *  
     * @author mcr70
     */
    public class Task {
        private ScheduledFuture<?> scheduledFuture;
        private long triggerTime;
        private Runnable runnable;

        private Task(ScheduledFuture<?> scheduledFuture, long triggerTime, Runnable r) {
            this.scheduledFuture = scheduledFuture;
            this.triggerTime = triggerTime;
            this.runnable = r;
        }

        /**
         * Cancel this Task. Canceling of a task removes it from 
         * executor.
         */
        public void cancel() {
            scheduledFuture.cancel(false); // do not interrupt
        }
        
        /**
         * Resets time of this Task. Application is expected to call this method
         * at period less than watchdog trigger time used when Task was created.
         * I.e. Once reset is called, trigger time is reset to initial value.
         */
        public void reset() {
            scheduledFuture.cancel(false); // do not interrupt
            this.scheduledFuture = ses.schedule(runnable, triggerTime, TimeUnit.MILLISECONDS);
        }
    }
    
    public interface Listener {
        /**
         * Called when an application failed to call reset() method of Task
         * before trigger time.
         * 
         * @see Task#reset()
         */
        void triggerTimeMissed();
    }
}
