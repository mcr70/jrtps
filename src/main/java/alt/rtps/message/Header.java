package alt.rtps.message;

import org.omg.CORBA.VersionSpecHelper;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.ProtocolVersion_t;
import alt.rtps.types.VendorId_t;

/**
 * 
 * @author mcr70
 *
 */
public class Header {
	private static final byte[] HDR_START = new byte[] {'R', 'T', 'P', 'S'};
	//private ProtocolId_t protocol;
	private byte[] hdrStart;
	private ProtocolVersion_t version;
	private VendorId_t vendorId;
	private GuidPrefix_t guidPrefix;

	public Header(GuidPrefix_t prefix) {
		hdrStart = HDR_START;
		version = ProtocolVersion_t.PROTOCOLVERSION_2_1;
		vendorId = VendorId_t.VENDORID_JRTPS; // VENDORID_UNKNOWN;
		guidPrefix = prefix;
	}


	Header(RTPSByteBuffer is) {
		// Header length == 20
		hdrStart = new byte[4];
		is.read(hdrStart);
		version = new ProtocolVersion_t(is);
		vendorId = new VendorId_t(is);
		guidPrefix = new GuidPrefix_t(is);
	}



	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(hdrStart);
		version.writeTo(buffer);
		vendorId.writeTo(buffer);
		guidPrefix.writeTo(buffer);
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

	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}

	public VendorId_t getVendorId() {
		return vendorId;
	}
	
	public ProtocolVersion_t getVersion() {
		return version;
	}
}
