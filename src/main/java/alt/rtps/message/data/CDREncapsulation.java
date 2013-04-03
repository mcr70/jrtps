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


	CDREncapsulation(RTPSByteBuffer bb) {
		this.bb = bb;
		this.options = (short) bb.read_short();
	}
	
	public CDREncapsulation(int size) {
		this.options = 0;
		this.bb = new RTPSByteBuffer(ByteBuffer.allocate(size));
		bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		
		bb.write(CDR_LE_HEADER);
		bb.write_short(options); // bb is positioned to start of actual data
	}

	@Override
	public boolean containsData() {
		return true;
	}

	@Override
	public byte[] getSerializedPayload() {
		byte[] serializedPayload = new byte[bb.position()];
		System.arraycopy(bb.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);
		
		return serializedPayload;
	}

	
	public RTPSByteBuffer getBuffer() {
		return bb;
	}
}
