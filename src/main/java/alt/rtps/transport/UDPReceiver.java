package alt.rtps.transport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

import alt.rtps.message.Message;
import alt.rtps.structure.Participant;
import alt.rtps.types.Locator_t;


/**
 * This class receives network data. It is configured according to Locator_t given
 * on constructor.
 * 
 * @author mcr70
 * @see 8.3.4 The RTPS Message Receiver
 */
public class UDPReceiver implements Runnable {
	private static final Logger log = Logger.getLogger(UDPReceiver.class);
	
	private final RTPSMessageBroker broker;
	private final Locator_t locator;
	
	private boolean running = true;

	public UDPReceiver(Locator_t locator, Participant p) throws SocketException {
		this.locator = locator;
		broker = new RTPSMessageBroker(p);
		
		log.debug("Listening on " + locator);
	}
	
	public void run() {
		try {
			DatagramSocket socket = null;
			if (locator.getInetAddress().isMulticastAddress()) {
				socket = new MulticastSocket(locator.getPort());
				((MulticastSocket)socket).joinGroup(locator.getInetAddress());
			}
			else {
				socket = new DatagramSocket(locator.getPort());
			}
			
			byte[] buf = new byte[16384];
			
			int i = 0;	
			while(running) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				socket.receive(p);
				
				// TODO: We could put msg into BlockingQueue and go immediately back to receiving
				try {
					Message msg = parseMessage(p.getData(), p.getLength());
					log.debug("Parsed RTPS message from " + locator + ": " + msg);
					broker.handleMessage(msg);
				}
				catch(Exception e) {
					log.error("Failed to parse message of length " + p.getLength(), e);
					//dumpMessage(i++, p.getData(), p.getLength()); // TODO: remove this
				}
				
			}
						
//			ch.setOption(StandardSocketOptions.SO_REUSEADDR, true);
//			ch.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private Message parseMessage(byte[] array, int length) {
		Message msg = null;
		try {
			msg = new Message(new RTPSByteBuffer(new ByteArrayInputStream(array, 0, length)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg;
	}

	public void stop() {
		running = false;
	}


	private void dumpMessage(int i, byte[] data, int length) {
		try {
			FileOutputStream fos = new FileOutputStream(new File("tmp/msg_" + i + ".bin"));
			fos.write(data, 0, length);
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Locator_t getLocator() {
		return locator;
	}
}
