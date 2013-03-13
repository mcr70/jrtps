package alt.rtps.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.transport.RTPSByteBuffer;

public class ParameterListEncapsulation extends DataEncapsulation {
	private ParameterList parameters;

	public ParameterListEncapsulation(ParameterList parameters) {
		this.parameters = parameters;
	}


	@Override
	public boolean containsData() {
		return true; // TODO: how do we represent key in serialized payload
	}

	@Override
	public byte[] getSerializedPayload() {

		ByteBuffer buffer = ByteBuffer.allocate(1024); // TODO: hardcoded
		buffer.order(ByteOrder.LITTLE_ENDIAN); // TODO: hardcoded

		RTPSByteBuffer bb = new RTPSByteBuffer(buffer);
		bb.write(PL_CDR_LE_HEADER);

		parameters.writeTo(bb);

		bb.align(4);
	
//		serializedPayload = new byte[buffer.position()];
//		System.arraycopy(buffer.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);
		return bb.getBuffer().array();
	}

}
