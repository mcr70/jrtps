package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.ProtocolVersion_t;
import alt.rtps.types.VendorId_t;

/**
 * 
 * @author mcr70
 * @see 9.4.5.10 InfoSource Submessage, 8.3.7.9 InfoSource
 */
public class InfoSource extends SubMessage {
	public static final int KIND = 0x0c;
	
	/**
	 * Indicates the protocol used to encapsulate subsequent Submessages.
	 */
	private ProtocolVersion_t protocolVersion;
	/**
	 * Indicates the VendorId of the vendor that encapsulated subsequent Submessages.
	 */
	private VendorId_t vendorId;
	/**
	 * Identifies the Participant that is the container of the RTPS Writer
	 * entities that are the source of the Submessages that follow.
	 */
	private GuidPrefix_t guidPrefix;

	public InfoSource(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	public ProtocolVersion_t getProtocolVersion() {
		return protocolVersion;
	}
	
	public VendorId_t getVendorId() {
		return vendorId;
	}
	
	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}
	
	public String toString() {
		return super.toString() + ", " + guidPrefix;
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
}
