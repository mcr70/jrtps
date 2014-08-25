package net.sf.jrtps.transport.mem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.transport.Transmitter;

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
        buffer.getBuffer().flip();

        outQueue.add(buffer.getBuffer().array());

        return overFlowed;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub        
    }
}
