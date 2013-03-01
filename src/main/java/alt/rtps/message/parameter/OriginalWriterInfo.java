package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class OriginalWriterInfo extends Parameter {
	OriginalWriterInfo() {
		super(ParameterEnum.PID_ORIGINAL_WRITER_INFO);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}