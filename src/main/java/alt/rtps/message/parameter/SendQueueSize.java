package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class SendQueueSize extends Parameter {
	SendQueueSize() {
		super(ParameterEnum.PID_SEND_QUEUE_SIZE);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}