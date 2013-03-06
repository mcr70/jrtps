package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;


public class QosPartition extends Parameter implements QualityOfService {
	QosPartition() {
		super(ParameterEnum.PID_PARTITION);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
	}
	
	public String toString() {
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}
}