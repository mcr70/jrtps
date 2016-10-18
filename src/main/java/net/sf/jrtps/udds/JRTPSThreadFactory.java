package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * A ThreadFactory, that sets names of the threads.
 * 
 * @author mcr70
 */
class JRTPSThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count = 0;
    private List<Thread> threads = new LinkedList<>();
    
    
    public JRTPSThreadFactory(int domainId) {
        prefix = "jrtps-d" + domainId + "-t";
    }
    
    @Override
    public synchronized Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + count++);
        thread.setDaemon(true);

        return thread;
    }

    /**
     * Called after participant close
     */
    @SuppressWarnings("deprecation")
	synchronized void stopThreads() {
        for (Thread t : threads) {
            t.stop();
        }
        
        threads.clear();
    }
}
