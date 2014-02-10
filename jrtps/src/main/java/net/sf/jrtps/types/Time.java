package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Represents a time in IETF Network Time Protocol (NTP) Standard (IETF RFC
 * 1305) format. see 9.3.2 Mapping of the Types that Appear Within Submessages
 * or Built-in Topic Data
 * 
 * @author mcr70
 */
public class Time {
    public static final int LENGTH = 8;

    public static final Time TIME_ZERO = new Time(0, 0);
    public static final Time TIME_INVALID = new Time(-1, 0xffffffff);
    public static final Time TIME_INFINITE = new Time(0x7fffffff, 0xffffffff);

    private final int seconds; // System.currentTimeMillis()
    private final int fraction;

    public Time(RTPSByteBuffer bb) {
        this.seconds = bb.read_long() & 0x7fffffff; // long
        this.fraction = bb.read_long(); // ulong
    }

    public Time(int sec, int frac) {
        seconds = sec;
        fraction = frac;
    }

    public Time(long systemCurrentMillis) {
        this.seconds = (int) (systemCurrentMillis / 1000);
        long scm = this.seconds * 1000;
        this.fraction = (int) (systemCurrentMillis - scm);
    }

    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(seconds);
        buffer.write_long(fraction);
    }

    public String toString() {
        return "Time_t[" + seconds + ":" + fraction + "], (" + String.format("0x%04x", seconds) + ":"
                + String.format("0x%04x", fraction) + ")";
    }

    public long timeMillis() {
        return this.seconds * 1000 + fraction;
    }
}
