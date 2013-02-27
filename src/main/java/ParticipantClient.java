import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import alt.rtps.message.Message;
import alt.rtps.structure.Participant;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;


public class ParticipantClient {
	public static void main(String[] args) throws IOException {
		//UDPListener u1 = new UDPListener(InetAddress.getLocalHost(), 58001);
		//u1.start();
		
		Participant p = new Participant(0);
		p.start();
	}
	
	
	static class UDPListener extends Thread {
		private final InetAddress addr;
		private final int port;

		public UDPListener(InetAddress addr, int port) {
			this.addr = addr;
			this.port = port;
		}

	
		public void run() {
			DatagramSocket socket = null;
			if(addr.isMulticastAddress()) {
				try {
					socket = new MulticastSocket(port);
					((MulticastSocket)socket).joinGroup(addr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				try {
					socket = new DatagramSocket(port);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			byte[] buf = new byte[16384];
			boolean running = true;
			int count = 3;
			while(running) {
				System.out.println("--- Receive UDP packets from " + addr + ", " + port);
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(p);
					System.out.println("---   Got " + p.getLength() + " UDP packet");
					dumpMessage(0, p.getData(), p.getLength());
					Message msg = parseMessage(p.getData(), p.getLength());
					System.out.println("---   Message was parsed: " + msg);
					
					if (count-- <= 0) {
						running = false;
					}
				} catch (IOException e) {
					running = false;
					e.printStackTrace();
				}
			}
			
			System.out.println("Exiting " + addr + ":" + port);
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
		private void dumpMessage(int i, byte[] data, int length) {
			try {
				File f = new File("tmp/udp_" + i + ".bin");
				System.out.println("dump udp packet to " + f.getAbsolutePath());
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(data, 0, length);
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
