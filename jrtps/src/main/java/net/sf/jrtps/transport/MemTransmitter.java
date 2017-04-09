package net.sf.jrtps.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.Message;

/**
 * A Transmitter that writers RTPS Messages into <i>BlockingQueue</i> 
 * 
 * @author mcr70
 */
public class MemTransmitter implements Transmitter {

    private BlockingQueue<byte[]> outQueue;
    private int bufferSize;

    public MemTransmitter(BlockingQueue<byte[]> outQueue, int bufferSize) {
        this.outQueue = outQueue;
        this.bufferSize = bufferSize;
    }

    @Override
    public boolean sendMessage(Message msg) {
        RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(bufferSize));
        buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
        boolean overFlowed = msg.writeTo(buffer);
        byte[] outputBytes = new byte[buffer.position()];
        byte[] array = buffer.getBuffer().array();
        
        System.arraycopy(array, 0, outputBytes, 0, outputBytes.length);
        
        outQueue.add(outputBytes);

        return overFlowed;
    }
}
