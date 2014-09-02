package net.sf.jrtps.transport.udp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.transport.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDPTransmitter send Messages to remote entities using UDP protocol.
 * 
 * @author mcr70
 */
public class UDPTransmitter implements Transmitter {
    private static final Logger log = LoggerFactory.getLogger(UDPTransmitter.class);
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
     * were succesfully written will be sent.
     * 
     * @param m Message to send
     * @return true, if Message did not fully fit into buffer of this UDPWriter
     */
    public boolean sendMessage(Message m) {
        RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(bufferSize));
        buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
        boolean overFlowed = m.writeTo(buffer);
        buffer.getBuffer().flip();

        try {
            channel.write(buffer.getBuffer());
        } catch (ClosedByInterruptException cbie) {
            log.debug("Message sending was interrputed");
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

    public void close() throws IOException {
        channel.close();
    }
}
