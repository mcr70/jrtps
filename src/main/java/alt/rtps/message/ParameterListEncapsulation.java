package alt.rtps.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import alt.rtps.message.parameter.ParameterList;
import alt.rtps.transport.RTPSByteBuffer;

public class ParameterListEncapsulation extends DataEncapsulation {
	private ParameterList parameters;
	private final boolean littleEndian;

	public ParameterListEncapsulation(ParameterList parameters) {
		this(parameters, true);
	}
	
	public ParameterListEncapsulation(ParameterList parameters, boolean littleEndian) {
		this.parameters = parameters;
		this.littleEndian = littleEndian;
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
		RTPSByteBuffer bb = new RTPSByteBuffer(buffer);
		
		if (littleEndian) {
			buffer.order(ByteOrder.LITTLE_ENDIAN); 
			bb.write(PL_CDR_LE_HEADER);
		}
		else {
			buffer.order(ByteOrder.BIG_ENDIAN);
			bb.write(PL_CDR_BE_HEADER);
		}

		parameters.writeTo(bb);
		
		byte[] serializedPayload = new byte[buffer.position()];
		System.arraycopy(bb.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);
		
		return serializedPayload;
	}
}
