package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Duration_t.
 * 
 * @author mcr70
 *
 */
public class Duration_t {
	// TODO: check infinite duration
	public static final Duration_t INFINITE = new Duration_t(Integer.MAX_VALUE, 0);
	public int sec;
	public int nano;

	/**
	 * Constructor for Duration_t
	 * @param millis Duration expressed in milliseconds.
	 */
	public Duration_t(int millis) {
		this.sec = (int) (millis / 1000);
		this.nano = 0;
	}
	
	/**
	 * Constructor for Duration_t
	 * @param sec seconds
	 * @param nano nanoseconds
	 */
	public Duration_t(int sec, int nano) {
		this.sec = sec;
		this.nano = nano;
	}
	
	/**
	 * Constructs Duration_t from RTPSByteBuffer.
	 * @param bb 
	 */
	public Duration_t(RTPSByteBuffer bb) {
		sec = bb.read_long();
		nano = bb.read_long();
	}
	
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(sec);
		buffer.write_long(nano);
	}

	/**
	 * Gets this duration as milliseconds.
	 * 
	 * @return duration as milliseconds
	 */
	public long asMillis() {
		long n = 0;
		if (nano != 0) {
			n = nano / 1000000;
		}
		return (long)sec * 1000 + n;
	}

	public String toString() {
		return "[" + sec + ":" + nano + "]";
	}	
}
