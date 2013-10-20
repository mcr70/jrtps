package net.sf.jrtps.types;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * 
 * @author mcr70
 *
 */
public class Locator_t {
	public static final int LENGTH = 24;
	
	public static final int LOCATOR_KIND_UDPv4 = 1;
	public static final int LOCATOR_KIND_UDPv6 = 2;
	public static final int LOCATOR_KIND_INVALID = -1;
	
	protected static final int PB = 7400; // NOTE: These should be moved to somewhere else. default ports.
	protected static final int DG = 250;
	protected static final int PG = 2;
	protected static final int d0 = 0;  // used with metatraffic (discovery), multicast @see 9.6.1.1
	protected static final int d1 = 10; // used with metatraffic (discovery), unicast   @see 9.6.1.1
	protected static final int d2 = 1;  // Used with user traffic, multicast @see 9.6.1.2
	protected static final int d3 = 11; // Used with user traffic, unicast   @see 9.6.1.2
	
	private int kind;
	private int port;
	private byte[] address;
	

	public Locator_t(InetAddress addr, int port) {
		this.port = port;
		byte[] bytes = addr.getAddress();
		
		if (bytes.length == 4) {
			this.kind = LOCATOR_KIND_UDPv4;
			
			this.address = new byte[16];
			this.address[12] = bytes[0];
			this.address[13] = bytes[1];
			this.address[14] = bytes[2];
			this.address[15] = bytes[3];
		}
		else if (bytes.length == 16) {
			this.kind = LOCATOR_KIND_UDPv6;
			this.address = bytes;
		}
		else {
			throw new IllegalArgumentException("Invalid length for InetAddress.getAddress: " + bytes.length);
		}
	}
	
	public Locator_t(RTPSByteBuffer is) {
		this.kind = is.read_long();
		this.port = is.read_long();
		this.address = new byte[16];
	
		is.read(address);
	}
	
	private Locator_t(int kind, int port, byte[] address) {
		this.kind = kind;
		this.port = port;
		this.address = address;

		assert address != null && address.length == 16;
	}

	/**
	 * 
	 * @param domainId
	 * @see 9.6.1.2 User traffic
	 */
	public static Locator_t defaultUserMulticastLocator(int domainId) { 
		byte[] addr = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,(byte) 239,(byte) 255,0,1};
		
		return new Locator_t(LOCATOR_KIND_UDPv4, PB + DG * domainId + d2, addr);
	}
	
	/**
	 * 
	 * @param domainId
	 * @see 9.6.1.2 User traffic
	 */
	public static Locator_t defaultUserUnicastLocator(int domainId, int participantId) { 
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) { 
			addr = InetAddress.getLoopbackAddress();
		}

		return new Locator_t(addr, PB + DG * domainId + d3 + PG * participantId);
	}

	
	/**
	 * 
	 * @param domainId
	 * @see 9.6.1.2 User traffic
	 */
	public static Locator_t defaultMetatrafficUnicastLocator(int domainId, int participantId) { 
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) { 
			addr = InetAddress.getLoopbackAddress();
		}
		
		return new Locator_t(addr, PB + DG * domainId + d1 + PG * participantId);
	}
	
	/**
	 * 
	 * @param domainId
	 * @see 9.6.1.4 Default Settings for the Simple Participant Discovery Protocol
	 */
	public static Locator_t defaultDiscoveryMulticastLocator(int domainId) { 
		byte[] addr = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,(byte) 239,(byte) 255,0,1};
		
		return new Locator_t(LOCATOR_KIND_UDPv4, PB + DG * domainId + d0, addr);
	}


	public InetAddress getInetAddress() {
		InetAddress inetAddress = null;
		
		try {
			switch(kind) {
			case LOCATOR_KIND_UDPv4:
				byte[] addr = new byte[4];
				addr[0] = address[12];
				addr[1] = address[13];
				addr[2] = address[14];
				addr[3] = address[15];
				inetAddress = InetAddress.getByAddress(addr);
				break;
			case LOCATOR_KIND_UDPv6:
				inetAddress = InetAddress.getByAddress(address);
				break;
			default:
				throw new IllegalArgumentException("Internal error: Unknown Locator kind: 0x" + 
						String.format("%04x", kind) +", port: " + String.format("%04x", port));
			}
		}
		catch(UnknownHostException uhe) {
			// Not Possible. InetAddress.getByAddress throws this exception if byte[] length is not
			// 4 or 16. We have ensured this in constructor & switch-case
		}
		
		return inetAddress;
	}
	
	/**
	 * Get the remote socket address
	 * @return
	 */
	public SocketAddress getSocketAddress() {
		return new InetSocketAddress(getInetAddress(), port);
	}
	
	public int getPort() {
		// TODO: check this. port is ulong (32 bits), but in practice, 16 last bits (0-65535) is our port number
		//       For large port numbers (> 32767), we need to mask out negativeness   
		return port & 0xffff;
	}
	
	public String toString() {
		return getInetAddress() + ":" + getPort();
	}


	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
		buffer.write_long(port);
		buffer.write(address);
	}
}
