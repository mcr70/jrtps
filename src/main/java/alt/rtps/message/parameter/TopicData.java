package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class TopicData extends Parameter {
	TopicData() {
		super(ParameterEnum.PID_TOPIC_DATA);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}