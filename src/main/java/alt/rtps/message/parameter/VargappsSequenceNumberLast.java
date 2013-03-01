package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class VargappsSequenceNumberLast extends Parameter {
	VargappsSequenceNumberLast() {
		super(ParameterEnum.PID_VARGAPPS_SEQUENCE_NUMBER_LAST);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}