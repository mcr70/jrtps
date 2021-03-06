package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Duration.
 * 
 * @author mcr70
 */
public class Duration implements Comparable<Duration> {
    // INFINITE: dds v1.2, IDL :
    //    const long DURATION_INFINITE_SEC  = 0x7fffffff
    //    const long DURATION_INFINITE_NSEC = 0x7fffffff
    public static final Duration INFINITE = new Duration(Integer.MAX_VALUE, Integer.MAX_VALUE);
	//public static final Duration INFINITE = new Duration(Integer.MAX_VALUE, -1);
    private int sec;
    private int nano;

    /**
     * Constructor for Duration
     * 
     * @param millis Duration expressed in milliseconds. -1 represents INFINITE duration
     */
    public Duration(long millis) {
        if (millis == -1) { 
            this.sec = Integer.MAX_VALUE;
            this.nano = Integer.MAX_VALUE;
        }
        else {
            this.sec = (int) (millis / 1000);
            millis = millis % 1000;
            this.nano = (int) (millis * 1000000);
        }
    }

    /**
     * Constructor for Duration
     * 
     * @param sec seconds
     * @param nano nanoseconds
     */
    public Duration(int sec, int nano) {
        this.sec = sec;
        this.nano = nano;
    }
    
    /**
     * Reads Duration from RTPSByteBuffer.
     * 
     * @param bb RTPSByteBuffer to read Duration from
     */
    public Duration(RTPSByteBuffer bb) {
        sec = bb.read_long();
        nano = bb.read_long();
    }

    /**
     * Writes this Duration to given RTPSByteBuffer.
     * 
     * @param buffer RTPSByteBuffer to write to
     */
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
        return (long) sec * 1000 + n;
    }

    /**
     * Checks whether this Duration is infinite or not
     * @return true or false
     */
    public boolean isInfinite() {
        return sec == Integer.MAX_VALUE && nano == Integer.MAX_VALUE;
    }
    
    /**
     * Gets the sec attribute of this Duration
     * @return seconds
     */
    int getSeconds() { // package private. used for testing
        return sec;
    }
    
    /**
     * Gets the nano attribute of this Duration
     * @return Nano seconds
     */
    int getNanoSeconds() { // package private. used for testing
        return nano;
    }

    @Override
    public int compareTo(Duration o) {
        return (int) (asMillis() - o.asMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Duration) {
            Duration other = (Duration) o;
            if (sec == other.sec && nano == other.nano) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public String toString() {
        return "[" + sec + ":" + nano + "]";
    }
}
