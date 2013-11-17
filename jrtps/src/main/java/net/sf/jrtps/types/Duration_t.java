package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class Duration_t {
	public static final Duration_t INFINITE = new Duration_t(Integer.MAX_VALUE, 0);
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

	public long asMillis() {
		long n = 0;
		if (nano != 0) {
			n = nano / 1000000;
		}
		
		return (sec * 1000) + n;
	}
	
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(sec);
		buffer.write_long(nano);
	}
}
