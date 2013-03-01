package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class StatusInfo extends Parameter {
	private byte[] flags;

	public StatusInfo(byte[] flags) {
		super(ParameterEnum.PID_STATUS_INFO);
		this.flags = flags;
		
		if (flags.length != 4) {
			throw new RuntimeException("StatusInfo.flags must be of length 4");
		}
	}
	
	StatusInfo() {
		super(ParameterEnum.PID_STATUS_INFO);
	}


	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
		this.flags = getBytes();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write(flags);
	}
	
	public boolean isDisposed() {
		return (flags[3] & 0x1) == 0x1;
	}
	
	public boolean isUnregistered() {
		return (flags[3] & 0x2) == 0x2;
	}
}