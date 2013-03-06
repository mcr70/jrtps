package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;


public class QosPartition extends Parameter implements QualityOfService {
	private String[] partitions;
	
	QosPartition() {
		super(ParameterEnum.PID_PARTITION);
	}
	
	public QosPartition(String[] partitions) {
		super(ParameterEnum.PID_PARTITION);
		this.partitions = partitions;
		
		if (this.partitions == null) {
			this.partitions = new String[0];
		}
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		//readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
		int len = bb.read_long();
		this.partitions = new String[len];
		for (int i = 0; i < len; i++) {
			partitions[i] = bb.read_string();
		}
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		//writeBytes(bb); // TODO: default writing. just writes byte[] in super class
		bb.write_long(partitions.length);
		for (int i = 0; i < partitions.length; i++) {
			bb.write_string(partitions[i]);
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < partitions.length; i++) {
			sb.append(partitions[i]);
			if (i < partitions.length - 1) {
				sb.append(",");
			}
		}
		return super.toString() + "([" + sb + "])";
	}
}