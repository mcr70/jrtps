package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.ProtocolVersion_t;
import net.sf.jrtps.types.VendorId_t;

/**
 * The Header identifies the message as belonging to the RTPS protocol. The Header identifies the version of the protocol
 * and the vendor that sent the message.
 * @author mcr70
 *
 */
public class Header {
	private static final byte[] HDR_START = new byte[] {'R', 'T', 'P', 'S'};
	//private ProtocolId_t protocol;
	private byte[] hdrStart;
	private ProtocolVersion_t version;
	private VendorId_t vendorId;
	private GuidPrefix guidPrefix;

	/**
	 * Constructor for Header. ProtocolVersion is set to 2.1 and VendorId is set to jRTPS.
	 * @param prefix
	 */
	public Header(GuidPrefix prefix) {
		hdrStart = HDR_START;
		version = ProtocolVersion_t.PROTOCOLVERSION_2_1;
		vendorId = VendorId_t.VENDORID_JRTPS; // VENDORID_UNKNOWN;
		guidPrefix = prefix;
	}

	/**
	 * Constructs Header from given RTPSByteBuffer.
	 * @param bb
	 */
	Header(RTPSByteBuffer bb) {
		// Header length == 20
		hdrStart = new byte[4];
		bb.read(hdrStart);
		version = new ProtocolVersion_t(bb);
		vendorId = new VendorId_t(bb);
		guidPrefix = new GuidPrefix(bb);
	}

	/**
	 * Writer this Header to given RTPSByteBuffer.
	 * @param buffer
	 */
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(hdrStart);
		version.writeTo(buffer);
		vendorId.writeTo(buffer);
		guidPrefix.writeTo(buffer);
	}	
	
	/**
	 * Defines a default prefix to use for all GUIDs that appear in the message.
	 */
	public GuidPrefix getGuidPrefix() {
		return guidPrefix;
	}

	/**
	 * Indicates the vendor that provides the implementation of the RTPS protocol.
	 */
	public VendorId_t getVendorId() {
		return vendorId;
	}
	
	/**
	 * Identifies the version of the RTPS protocol.
	 */
	public ProtocolVersion_t getVersion() {
		return version;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(new String(hdrStart));
		sb.append(", ");
		sb.append(version.toString());
		sb.append(", ");
		sb.append(vendorId.toString());
		sb.append(", ");
		sb.append(guidPrefix.toString());

		return sb.toString();
	}
}
