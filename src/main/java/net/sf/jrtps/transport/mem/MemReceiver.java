package net.sf.jrtps.transport.mem;

import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.transport.Receiver;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Receiver that receives bytes from a <i>BlockingQueue</i>. 
 * 
 * @author mcr70
 */
public class MemReceiver implements Receiver {
    private static final Logger logger = LoggerFactory.getLogger(MemReceiver.class);
    
    private final Locator locator;
    private final int participantId;
    private final BlockingQueue<byte[]> inQueue;
    private final BlockingQueue<byte[]> outQueue;
    
    private boolean running = true;
    
    
    public MemReceiver(Locator locator, int participantId, BlockingQueue<byte[]> inQueue, BlockingQueue<byte[]> outQueue) {
        this.locator = locator;
        this.participantId = participantId;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        while(running) {
            try {
                byte[] bytes = inQueue.take();
                outQueue.put(bytes);
            } 
            catch (InterruptedException e) {
                logger.debug("Got interrupted, exiting");
                running = false;
            }
        }
    }

    @Override
    public Locator getLocator() {
        return locator;
    }

    @Override
    public int getParticipantId() {
        return participantId;
    }

    @Override
    public void close() {
        running = false;
    }
    
}
