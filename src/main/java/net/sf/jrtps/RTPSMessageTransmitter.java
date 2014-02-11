package net.sf.jrtps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.transport.UDPWriter;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSMessageTransmitter is responsible for sending RTPS messages to its
 * destination. RTPSWriters write their RTPS Messages into BlockingQueue. This
 * class reads the queue and possibly joins multiple messages into a single RTPS
 * message, that gets sent to wire.
 * 
 * @see RTPSWriter
 * @author riekmi
 */
class RTPSMessageTransmitter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RTPSMessageTransmitter.class);
    private final BlockingQueue<Message> queue;
    private final UDPWriter writer;
    private final RTPSByteBuffer buffer;

    RTPSMessageTransmitter(Locator locator, BlockingQueue<Message> queue, int bufferSize) throws IOException {
        this.queue = queue;
        this.writer = new UDPWriter(locator, bufferSize); // TODO: remove
                                                          // bufferSize from
                                                          // constructor
        this.buffer = new RTPSByteBuffer(ByteBuffer.allocate(bufferSize));
        this.buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            try {
                Message msg = queue.take();
                // TODO: Prepare message buffer here, and if there is no
                // overflow, append next Message also
                // until there are no more messages, or an overflow occurs
                boolean overFlowed = msg.writeTo(buffer);
                GuidPrefix currentPrefix = msg.getHeader().getGuidPrefix();
                queue.poll();

                writer.sendMessage(msg);
            } catch (InterruptedException e) {
                running = false;
            }
        }

        logger.debug("RTPSMessageTransmitter exiting");
        try {
            writer.close();
        } catch (IOException e) {
            logger.warn("Exception occured while closing UDPWriter", e);
        }
    }
}
