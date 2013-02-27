package alt.rtps.types;

import alt.rtps.transport.RTPSByteBuffer;

public class Duration_t {
	public int sec;
	public int nano;

	public Duration_t(int sec, int nano) {
		this.sec = sec;
		this.nano = nano;
	}
	
	
	public Duration_t(RTPSByteBuffer bb) {
		sec = bb.read_long();
		nano = bb.read_long();
	}
	
	public String toString() {
		return "[" + sec + ":" + nano + "]";
	}


	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(sec);
		buffer.write_long(nano);
	}
}
