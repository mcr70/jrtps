package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Time_t;

/**
 * Provides a source timestamp for subsequent Entity Submessages.
 * In order to implement the DDS_BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS policy, implementations
 * must include an InfoTimestamp Submessage with every update from a Writer.
 * 
 * @author mcr70
 * @see 8.3.7.9.6 InfoTimestamp
 */
public class InfoTimestamp extends SubMessage {
	public static final int KIND = 0x09;

	/**
	 * Present only if the InvalidateFlag is not set in the header.
	 * Contains the timestamp that should be used to interpret the subsequent Submessages.
	 */
	private Time_t timestamp;


	public InfoTimestamp(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	public InfoTimestamp(Time_t timestamp) {
		super(new SubMessageHeader(KIND));

		this.timestamp = timestamp;
	}

	/**
	 * Indicates whether subsequent Submessages should be considered as having a timestamp or not.
	 * Timestamp is present in _this_ submessage only if the InvalidateFlag is not set in the header.
	 */
	public boolean invalidateFlag() {
		return (header.flags & 0x2) != 0;
	}


	
	private void readMessage(RTPSByteBuffer bb) {
		if (!invalidateFlag()) {
			this.timestamp = new Time_t(bb);
		}
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		if (!invalidateFlag()) {
			timestamp.writeTo(buffer);
		}
	}


	public Time_t getTimeStamp() {
		return timestamp;
	}
	
	public String toString() {
		return super.toString() + ", " + timestamp;
	}
}
