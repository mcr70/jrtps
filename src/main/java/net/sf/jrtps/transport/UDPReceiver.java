package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class receives network data. It is configured according to Locator given
 * on constructor.
 * <p>
 * see 8.3.4 The RTPS Message Receiver
 * 
 * @author mcr70
 * 
 */
public class UDPReceiver implements Runnable, Receiver {
    private static final Logger log = LoggerFactory.getLogger(UDPReceiver.class);

    private final Semaphore initLock = new Semaphore(1);
    private final BlockingQueue<byte[]> queue;
    private final DatagramSocket socket;
    
    private boolean running = true;

    private int bufferSize;

    UDPReceiver(DatagramSocket ds, BlockingQueue<byte[]> queue, int bufferSize) {
        this.socket = ds;
        this.queue = queue;
        this.bufferSize = bufferSize;
    }

    public void run() {
        log.debug("Starting to listen on port {}", socket.getLocalPort());
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

    public void close() {
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
