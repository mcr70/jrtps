package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.jrtps.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDPTransmitter send Messages to remote entities using UDP protocol.
 * 
 * @author mcr70
 */
public class UDPTransmitter implements Transmitter {
	private static final Logger log = LoggerFactory.getLogger(UDPTransmitter.class);

	// This queue is shared among all the UDPTransmitters. Expected size is 1.
	// This is used to minimize ByteBuffer allocations
	private static ConcurrentLinkedQueue<RTPSByteBuffer> bufferQueue = new ConcurrentLinkedQueue<>();

	private final UDPLocator locator;
    private final DatagramChannel channel;
    private final int bufferSize;

    /**
     * Constructor for UDPWriter.
     * @param locator Locator where the messages will be sent.
     * @param bufferSize Size of the buffer that will be used to write messages. 
     * @throws IOException
     */
    UDPTransmitter(UDPLocator locator, int bufferSize) throws IOException {
        this.locator = locator;
        this.bufferSize = bufferSize;
        channel = DatagramChannel.open();
        channel.connect(locator.getSocketAddress());
    }

   
    
    /**
     * Sends a Message to a Locator of this UDPWriter.
     * If an overflow occurs during writing of Message, only submessages that
     * were successfully written will be sent.
     * 
     * @param m Message to send
     * @return true, if Message did not fully fit into buffer of this UDPWriter
     */
    @Override
    public boolean sendMessage(Message m) {
    	// Try to use cached buffer, primary reason is for avoiding buffer allocation
    	RTPSByteBuffer buffer = bufferQueue.poll(); 
    	if (buffer == null) {
    		buffer = new RTPSByteBuffer(ByteBuffer.allocate(bufferSize));
            buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
    	}
    	
        boolean overFlowed = m.writeTo(buffer);
        buffer.getBuffer().flip();

        try {
            channel.write(buffer.getBuffer());
            if (bufferQueue.size() == 0) { // Add buffer to cache to be re-used
            	buffer.getBuffer().clear();
            	bufferQueue.add(buffer);
            }
        } catch (ClosedByInterruptException cbie) {
            log.debug("Message sending was interrupted");
        } catch (IOException e) {
            log.error("Failed to send message to " + locator, e);
        }
        
        return overFlowed;
    }

    @SuppressWarnings("unused")
    private void writeToFile(Buffer buffer, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.getChannel().write((ByteBuffer) buffer);
            fos.close();
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    /**
     * Close this UDPTransmitter.
     */
    void close() {
    	try {
			channel.close();
		} 
    	catch (IOException e) {
			log.warn("Failed to close UDPTransmitter", e);
    	}
    }



	boolean isOpen() {
		return channel.isOpen();
	}
}
