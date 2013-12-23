package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class receives network data. It is configured according to Locator given
 * on constructor. <p> 
 * see 8.3.4 The RTPS Message Receiver
 * 
 * @author mcr70
 * 
 */
public class UDPReceiver implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(UDPReceiver.class);

	private final Semaphore initLock = new Semaphore(1);
	private final BlockingQueue<byte[]> queue;
	private final Locator locator;

	private boolean running = true;
	DatagramSocket socket = null;

	private int bufferSize;


	public UDPReceiver(Locator locator, BlockingQueue<byte[]> queue, int bufferSize) throws SocketException {
		this.locator = locator;
		this.queue = queue;
		this.bufferSize = bufferSize;
	}

	public void run() {
		try {
			initLock.acquire();
		} 
		catch (InterruptedException e1) {
			log.debug("Failed to acquire lock duringin initialization. exiting.");
			return;
		}
		
		try {
			if (locator.getInetAddress().isMulticastAddress()) {
				socket = new MulticastSocket(locator.getPort());
				((MulticastSocket)socket).joinGroup(locator.getInetAddress());
			}
			else {
				socket = new DatagramSocket(locator.getPort());
			}
		}
		catch(IOException ioe) {
			log.warn("Got IOException during creation of socket", ioe);
		}

		log.debug("Listening on {}", locator);
		initLock.release();


		byte[] buf = new byte[bufferSize];

		while(running) {
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try {	
				socket.receive(p);

				byte[] bytes = new byte[p.getLength()];
				System.arraycopy(p.getData(), 0, bytes, 0, bytes.length);
				log.debug("Received {} bytes from {}", bytes.length, locator);
				
				queue.put(bytes);
			}
			catch(IOException se) {
				// Ignore. If we are still running, try to receive again
			}
			catch(InterruptedException ie) {
				running = false;
			}
		} // while(...)
	}


	public void close() {
		log.debug("Closing {}, {}", locator, socket);
		try {
			initLock.acquire();
			if (socket != null) {
				socket.close();
			}
			running = false;
		} 
		catch (InterruptedException e) {
			log.debug("close() was interrupted");
		}
	}


	public Locator getLocator() {
		return locator;
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
