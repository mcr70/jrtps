package net.sf.jrtps.types;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class LocatorUDPv4_t {
	public static final LocatorUDPv4_t LOCATORUDPv4_INVALID = new LocatorUDPv4_t(0,0);
	/**
	 * The mapping between the dot-notation 'a.b.c.d' of an IPv4 address and its representation as an unsigned
	 * long is as follows: address = (((a*256 + b)*256) + c)*256 + d
	 */
	private final int address;
	private final int port;
	private InetAddress inetAddress;

	private LocatorUDPv4_t(int addr, int port) {
		this.address = addr;
		this.port = port;
	}
	
	public LocatorUDPv4_t(RTPSByteBuffer bb) {
		address = bb.read_long();
		port = bb.read_long();
	}
	
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(address);
		buffer.write_long(port);
	}


	public InetAddress getInetAddress() {
		if (inetAddress == null) {
			byte[] bytes = new byte[4];
			bytes[0] = (byte) (this.address >> 24);
			bytes[1] = (byte) (this.address >> 16);
			bytes[2] = (byte) (this.address >> 8);
			bytes[3] = (byte) (this.address & 0xff);

			try {
				inetAddress = InetAddress.getByAddress(bytes);
			} catch (UnknownHostException e) {
				// Not Possible. gets thrown if bytes.length is not 4 or 16
			}
		}

		return inetAddress;
	}
}
