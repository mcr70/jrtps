package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;

public class UnknownParameter extends Parameter {
	private final short paramId;

	protected UnknownParameter(short paramId) {
		super(ParameterEnum.PID_UNKNOWN_PARAMETER);

		this.paramId = paramId;
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length);
	}
	
	public String toString() {
		return super.toString() + ", ID " + paramId + ": " + Arrays.toString(getBytes());
	}
}
