package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.types.Locator;

/**
 * This class receives UDP packets from the network. 
 * 
 * @author mcr70
 */
public class UDPReceiver implements Receiver {
    private static final Logger log = LoggerFactory.getLogger(UDPReceiver.class);

    private final BlockingQueue<byte[]> queue;
    private final DatagramSocket socket;
    private final int bufferSize;
    private final UDPLocator locator;

    private boolean running = true;
    
    UDPReceiver(UDPLocator locator, ReceiverConfig rConfig, BlockingQueue<byte[]> queue, int bufferSize) throws UnknownHostException {        
        this.locator = locator;
		this.socket = rConfig.ds;
        this.queue = queue;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        log.debug("Listening on {}:{}", locator.getUri());
        
        byte[] buf = new byte[bufferSize];

        while (running) {
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(p);

                byte[] bytes = new byte[p.getLength()];
                System.arraycopy(p.getData(), 0, bytes, 0, bytes.length);
                log.debug("Received {} bytes from port {}", bytes.length, socket.getLocalPort());

                queue.put(bytes);
            } catch (IOException se) {
                // Ignore. If we are still running, try to receive again
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                running = false;
            }
        } // while(...)
    }
    
    void close() {
        log.debug("Closing {}", socket.getLocalPort());
        
        if (socket != null) {
            socket.close();
        }
        running = false;
    }

    @SuppressWarnings("unused")
    private void writeMessage(String string, byte[] msgBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(string);
            fos.write(msgBytes, 0, msgBytes.length);
            fos.close();
        } catch (Exception e) {
            log.error("Failed to write message to {}", string, e);
        }
    }
}
