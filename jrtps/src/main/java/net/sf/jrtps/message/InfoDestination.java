package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

/**
 * This message is sent from an RTPS Writer to an RTPS Reader to modify the
 * GuidPrefix used to interpret the Reader entityIds appearing in the
 * Submessages that follow it.
 * 
 * see 8.3.7.7 InfoDestination
 * 
 * @author mcr70
 * 
 */
public class InfoDestination extends SubMessage {
    public static final int KIND = 0x0e;

    private GuidPrefix guidPrefix;

    /**
     * Sets GuidPrefix_t to UNKNOWN.
     */
    public InfoDestination() {
        this(GuidPrefix.GUIDPREFIX_UNKNOWN);
    }

    /**
     * This constructor is used when the intention is to send data into network.
     * 
     * @param guidPrefix GuidPrefix of InfoDestination
     */
    public InfoDestination(GuidPrefix guidPrefix) {
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

        this.guidPrefix = new GuidPrefix(bb);
    }

    /**
     * Provides the GuidPrefix that should be used to reconstruct the GUIDs of
     * all the RTPS Reader entities whose EntityIds appears in the Submessages
     * that follow.
     * 
     * @return GuidPrefix
     */
    public GuidPrefix getGuidPrefix() {
        return guidPrefix;
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        guidPrefix.writeTo(bb);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + guidPrefix;
    }
}
