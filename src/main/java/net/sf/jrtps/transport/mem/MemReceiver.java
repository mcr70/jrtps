package net.sf.jrtps.transport.mem;

import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.transport.Receiver;
import net.sf.jrtps.types.Locator;

public class MemReceiver implements Receiver {

    private BlockingQueue<byte[]> inQueue;
    private BlockingQueue<byte[]> outQueue;
    private int participantId;

    public MemReceiver(int participantId, BlockingQueue<byte[]> inQueue, BlockingQueue<byte[]> outQueue) {
        this.participantId = participantId;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Locator getLocator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getParticipantId() {
        return participantId;
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
