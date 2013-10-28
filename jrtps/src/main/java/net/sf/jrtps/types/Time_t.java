package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;


/**
 * Represents a time in IETF Network Time Protocol (NTP) Standard (IETF RFC 1305) format.
 * 
 * @author mcr70
 * @see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in Topic Data
 */
public class Time_t {
	public static final int LENGTH = 8;

	public static final Time_t TIME_ZERO = new Time_t(0,0);
	public static final Time_t TIME_INVALID = new Time_t(-1, 0xffffffff);
	public static final Time_t TIME_INFINITE = new Time_t(0x7fffffff, 0xffffffff);
	
	private final int seconds; // System.currentTimeMillis()
	private final int fraction;
	
	
	public Time_t(RTPSByteBuffer bb) {
		this.seconds = bb.read_long() & 0x7fffffff;  // long
		this.fraction = bb.read_long(); // ulong
	}

	public Time_t(int sec, int frac) {
		seconds = sec;
		fraction = frac;
	}

	public Time_t(long systemCurrentMillis) {
		this.seconds = (int) (systemCurrentMillis / 1000); 
		long scm = this.seconds * 1000;
		this.fraction = (int) (systemCurrentMillis - scm);
	}
	
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(seconds);
		buffer.write_long(fraction);
	}

	public String toString() {
		return "Time_t[" + seconds + ":" + fraction + "], (" + 
				String.format("0x%04x", seconds) + ":" + String.format("0x%04x", fraction) + ")";
	}
}
