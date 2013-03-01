package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class Sentinel extends Parameter {
	public Sentinel() {
		super(ParameterEnum.PID_SENTINEL);
	}
	

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		//buffer.write_short(getParameterId().kind());
		//System.out.println("*** " + buffer.position() + ", " + buffer.position() %4);
		//buffer.write_short(2);
		// NO LENGTH
	}
}