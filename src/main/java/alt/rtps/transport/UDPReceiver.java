package alt.rtps.transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.RTPSParticipant;
import alt.rtps.message.Message;
import alt.rtps.types.Locator_t;


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

	public UDPReceiver(Locator_t locator, RTPSParticipant p) throws SocketException {
		this.locator = locator;
		broker = new RTPSMessageBroker(p);
		
		log.debug("Listening on {}", locator);
	}
	
	public void run() {
		DatagramSocket socket = null;
		try {
			if (locator.getInetAddress().isMulticastAddress()) {
				socket = new MulticastSocket(locator.getPort());
				((MulticastSocket)socket).joinGroup(locator.getInetAddress());
			}
			else {
				socket = new DatagramSocket(locator.getPort());
			}
			
			byte[] buf = new byte[16384];
				
			while(running) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				socket.receive(p);
				
				try {	
					byte[] msgBytes = new byte[p.getLength()];
					System.arraycopy(p.getData(), 0, msgBytes, 0, msgBytes.length);
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
			log.error("Got SocketException. Closing.", e); 
		} 
		catch (IOException e) {
			log.error("Got IOException. Closing.", e);
		}
		finally {
			socket.close();
		}
	}
	

	public void stop() {
		running = false;
	}


	public Locator_t getLocator() {
		return locator;
	}
}
