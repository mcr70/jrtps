package alt.rtps.message.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import alt.rtps.message.parameter.ParameterList;
import alt.rtps.transport.RTPSByteBuffer;

/**
 * ParameterListEncapsulation is a specialization of DataEncapsulation which is used 
 * by discovery.
 * 
 * @author mcr70
 *
 */
public class ParameterListEncapsulation extends DataEncapsulation {
	private ParameterList parameters;
	private final boolean littleEndian;
	private short options;

	public ParameterListEncapsulation(ParameterList parameters) {
		this.parameters = parameters;
		this.options = 0;
		this.littleEndian = true;
	}
	
	ParameterListEncapsulation(RTPSByteBuffer bb) {
		this.options = (short) bb.read_short();
		this.parameters = new ParameterList(bb);
		this.littleEndian = bb.getBuffer().order().equals(ByteOrder.LITTLE_ENDIAN);
	}


	public ParameterList getParameterList() {
		return parameters;
	}
	
	@Override
	public boolean containsData() {
		return true; // TODO: how do we represent key in serialized payload
	}

	@Override
	public byte[] getSerializedPayload() {

		ByteBuffer buffer = ByteBuffer.allocate(1024); // TODO: hardcoded
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		RTPSByteBuffer bb = new RTPSByteBuffer(buffer);
		
		bb.write(PL_CDR_LE_HEADER);
		
		parameters.writeTo(bb);
		
		byte[] serializedPayload = new byte[buffer.position()];
		System.arraycopy(bb.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);
		
		return serializedPayload;
	}
}
