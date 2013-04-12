package alt.rtps.types;

import alt.rtps.transport.RTPSByteBuffer;


/**
 * 
 * @author mcr70
 */
public class SequenceNumber_t {
	public static final int LENGTH = 8;
	
	private int high = 0; // TODO: store as long instead
	private int low = 0;

	public SequenceNumber_t (RTPSByteBuffer bb) {
		this.high = bb.read_long();
		this.low = bb.read_long();
	}
	
	public SequenceNumber_t (int _high, int _low) {
		high = _high;
		low = _low;
	} 
	
	public SequenceNumber_t(long seqNum) {
		low = (int) (seqNum & 0xffff); // TODO: check this
		high = (int) ((seqNum >> 32) & 0xffff); 
	}

	public long getAsLong() {
		return ((high << 32) | low);
	}
	
	public String toString() {
		return "" + getAsLong();
	}

	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(high);
		buffer.write_long(low);
	}
} 
