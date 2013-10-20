package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.ProtocolVersion_t;
import net.sf.jrtps.types.VendorId_t;

/**
 * This message modifies the logical source of the Submessages that follow.
 * 
 * @author mcr70
 * @see 9.4.5.10 InfoSource Submessage, 8.3.7.9 InfoSource
 */
public class InfoSource extends SubMessage {
	public static final int KIND = 0x0c;
	
	private ProtocolVersion_t protocolVersion;
	private VendorId_t vendorId;
	private GuidPrefix_t guidPrefix;

	public InfoSource(GuidPrefix_t guidPrefix) {
		super(new SubMessageHeader(KIND));
		
		this.protocolVersion = ProtocolVersion_t.PROTOCOLVERSION_2_1;
		this.vendorId = VendorId_t.VENDORID_JRTPS;
		this.guidPrefix = guidPrefix;
	}
	
	InfoSource(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	/**
	 * Indicates the protocol used to encapsulate subsequent Submessages.
	 */
	public ProtocolVersion_t getProtocolVersion() {
		return protocolVersion;
	}
	
	/**
	 * Indicates the VendorId of the vendor that encapsulated subsequent Submessages.
	 */
	public VendorId_t getVendorId() {
		return vendorId;
	}
	
	/**
	 * Identifies the Participant that is the container of the RTPS Writer
	 * entities that are the source of the Submessages that follow.
	 */
	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}
	
	
	private void readMessage(RTPSByteBuffer bb) {
		bb.read_long(); // unused
		
		protocolVersion = new ProtocolVersion_t(bb);
		vendorId = new VendorId_t(bb);
		guidPrefix = new GuidPrefix_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(0); // TODO: check this
		protocolVersion.writeTo(buffer);
		vendorId.writeTo(buffer);
		guidPrefix.writeTo(buffer);
	}
	
	public String toString() {
		return super.toString() + ", " + guidPrefix;
	}
}
