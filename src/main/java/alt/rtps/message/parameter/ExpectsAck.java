package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class ExpectsAck extends Parameter {
	ExpectsAck() {
		super(ParameterEnum.PID_EXPECTS_ACK);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}