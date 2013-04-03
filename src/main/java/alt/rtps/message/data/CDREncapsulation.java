package alt.rtps.message.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * CDREncapsulation. 
 * 
 * @author mcr70
 *
 */
public class CDREncapsulation extends DataEncapsulation {

	private final RTPSByteBuffer bb;
	private final short options;


	CDREncapsulation(RTPSByteBuffer bb, short options) {
		this.bb = bb;
		this.options = options;
	}
	
	public CDREncapsulation(int size) {
		this.bb = new RTPSByteBuffer(ByteBuffer.allocate(size));
		bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		
		bb.write_octet((byte) 0);
		bb.write_octet((byte) 1); // CDR_LE
		
		this.options = 0;
		bb.write_short(options);
	}

	@Override
	public boolean containsData() {
		return true;
	}

	@Override
	public byte[] getSerializedPayload() {
		return null;
	}

	
	public RTPSByteBuffer getBuffer() {
		return bb;
	}
}
