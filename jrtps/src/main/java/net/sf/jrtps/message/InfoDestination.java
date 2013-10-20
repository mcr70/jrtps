package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix_t;

/**
 * This message is sent from an RTPS Writer to an RTPS Reader to modify the GuidPrefix used to 
 * interpret the Reader entityIds appearing in the Submessages that follow it.
 * 
 * @author mcr70
 * @see 8.3.7.7 InfoDestination
 */
public class InfoDestination extends SubMessage {
	public static final int KIND = 0x0e;

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
	InfoDestination(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		this.guidPrefix = new GuidPrefix_t(bb);
	}
	

	/**
	 * Provides the GuidPrefix that should be used to reconstruct the
	 * GUIDs of all the RTPS Reader entities whose EntityIds appears in the Submessages that follow.
	 */
	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		guidPrefix.writeTo(buffer);
	}

	public String toString() {
		return super.toString() + ", " + guidPrefix;
	}
}
