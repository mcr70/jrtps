package net.sf.jrtps.udds;

import java.util.concurrent.ThreadFactory;

/**
 * A ThreadFactory, that sets names of the threads.
 * 
 * @author mcr70
 */
class JRTPSThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count = 0;
    
    public JRTPSThreadFactory(int domainId, int participantId) {
        prefix = "jrtps-" + domainId + "/" + participantId + "-t";
    }
    
    @Override
    public synchronized Thread newThread(Runnable r) {
        return new Thread(r, prefix + count++);
    }
}
