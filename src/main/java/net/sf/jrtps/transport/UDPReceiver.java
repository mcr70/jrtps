package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class receives UDP packets from the network. 
 * 
 * @author mcr70
 */
public class UDPReceiver implements Receiver {
    private static final Logger log = LoggerFactory.getLogger(UDPReceiver.class);

    private final BlockingQueue<byte[]> queue;
    private final DatagramSocket socket;
    private final URI uri;
    private final int bufferSize;
    private final Locator locator;
    
    private boolean running = true;

    
    UDPReceiver(URI uri, DatagramSocket ds, BlockingQueue<byte[]> queue, int bufferSize) throws UnknownHostException {
        this.uri = uri;
        this.socket = ds;
        this.queue = queue;
        this.bufferSize = bufferSize;
        this.locator = new Locator(InetAddress.getByName(uri.getHost()), ds.getLocalPort());
    }

    public void run() {
        log.debug("Listening on {}:{}", uri.getHost(), socket.getLocalPort());
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

    @Override
    public Locator getLocator() {
        return locator;
    }

    @Override
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
