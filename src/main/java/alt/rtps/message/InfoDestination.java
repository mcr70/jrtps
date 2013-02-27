package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GuidPrefix_t;

/**
 * This message is sent from an RTPS Writer to an RTPS Reader to modify the GuidPrefix used to 
 * interpret the Reader entityIds appearing in the Submessages that follow it.
 * 
 * @author mcr70
 * @see 8.3.7.7 InfoDestination
 */
public class InfoDestination extends SubMessage {
	public static final int KIND = 0x0e;
	/**
	 * Provides the GuidPrefix that should be used to reconstruct the
	 * GUIDs of all the RTPS Reader entities whose EntityIds appears in the Submessages that follow.
	 */
	private GuidPrefix_t guidPrefix;

	/**
	 * Sets GuidPrefix_t to UNKNOWN.
	 */
	public InfoDestination() {
		this(GuidPrefix_t.GUIDPREFIX_UNKNOWN);
	}

	/**
	 * This constructor is used when the intention is to send data into network.
	 * @param guidPrefix 
	 */
	public InfoDestination(GuidPrefix_t guidPrefix) {
		super(new SubMessageHeader(KIND));
		this.guidPrefix = guidPrefix;
	}
	
	/**
	 * This constructor is used when receiving data from network.
	 * 
	 * @param smh
	 * @param bb
	 */
	public InfoDestination(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}
	

	private void readMessage(RTPSByteBuffer bb) {
		this.guidPrefix = new GuidPrefix_t(bb);
	}

	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}

	public String toString() {
		return super.toString() + ", " + guidPrefix;
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		guidPrefix.writeTo(buffer);
	}
}
