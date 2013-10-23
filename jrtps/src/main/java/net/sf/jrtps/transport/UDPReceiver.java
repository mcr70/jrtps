package net.sf.jrtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;

import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.types.Locator_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class receives network data. It is configured according to Locator_t given
 * on constructor.
 * 
 * @author mcr70
 * @see 8.3.4 The RTPS Message Receiver
 */
public class UDPReceiver implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(UDPReceiver.class);
	
	private final RTPSMessageBroker broker;
	private final Locator_t locator;
	
	private boolean running = true;
	DatagramSocket socket = null;

	public UDPReceiver(Locator_t locator, RTPSParticipant p) throws SocketException {
		this.locator = locator;
		broker = new RTPSMessageBroker(p);
	}
	
	public void run() {
		try {
			if (locator.getInetAddress().isMulticastAddress()) {
				socket = new MulticastSocket(locator.getPort());
				((MulticastSocket)socket).joinGroup(locator.getInetAddress());
			}
			else {
				socket = new DatagramSocket(locator.getPort());
			}
			
			log.debug("Listening on {}", locator);
			
			byte[] buf = new byte[16384];
				
			while(running) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);

				socket.receive(p);
				
				try {	
					byte[] msgBytes = new byte[p.getLength()];
					System.arraycopy(p.getData(), 0, msgBytes, 0, msgBytes.length);
					
					//writeMessage("c:/tmp/jrtps-received.bin", msgBytes);
					
					// TODO: We could put msgBytes into BlockingQueue and go back to receiving
					Message msg = new Message(new RTPSByteBuffer(msgBytes));
					
					log.debug("Parsed RTPS message from {}: {}", locator, msg);
					broker.handleMessage(msg);
				}
				catch(Exception e) {
					log.warn("Failed to parse message of length " + p.getLength(), e);
				}
			}
		} 
		catch (SocketException e) {
			log.error("Got SocketException, {}", locator, e); 
		} 
		catch (IOException e) {
			log.error("Got IOException. Closing.", e);
		}
		finally {
			if (socket != null && socket.isConnected()) {
				socket.close();
			}
		}
	}
	

	public void close() {
		log.debug("Closing {}, {}", locator, socket);
		if (socket != null) {
			socket.close();
		}
		running = false;
	}


	public Locator_t getLocator() {
		return locator;
	}


	private void writeMessage(String string, byte[] msgBytes) {
		try {
			FileOutputStream fos = new FileOutputStream(string);
			fos.write(msgBytes, 0, msgBytes.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
