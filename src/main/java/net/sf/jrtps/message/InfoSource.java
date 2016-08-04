package net.sf.jrtps.message;

import net.sf.jrtps.message.parameter.ProtocolVersion;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

/**
 * This message modifies the logical source of the Submessages that follow.
 * 
 * see 9.4.5.10 InfoSource Submessage, 8.3.7.9 InfoSource
 * 
 * @author mcr70
 * 
 */
public class InfoSource extends SubMessage {
    public static final int KIND = 0x0c;

    private ProtocolVersion protocolVersion;
    private VendorId vendorId;
    private GuidPrefix guidPrefix;    
    
    public InfoSource(GuidPrefix guidPrefix) {
        super(new SubMessageHeader(KIND));

        this.protocolVersion = ProtocolVersion.PROTOCOLVERSION_2_1;
        this.vendorId = VendorId.JRTPS;
        this.guidPrefix = guidPrefix;
    }

    InfoSource(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    /**
     * Indicates the protocol used to encapsulate subsequent Submessages.
     * 
     * @return ProtocolVersion
     */
    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Indicates the VendorId of the vendor that encapsulated subsequent
     * Submessages.
     * 
     * @return VendorId
     */
    public VendorId getVendorId() {
        return vendorId;
    }

    /**
     * Identifies the Participant that is the container of the RTPS Writer
     * entities that are the source of the Submessages that follow.
     * 
     * @return GuidPrefix
     */
    public GuidPrefix getGuidPrefix() {
        return guidPrefix;
    }

    private void readMessage(RTPSByteBuffer bb) {
        bb.read_long(); // unused

        protocolVersion = new ProtocolVersion(bb);
        vendorId = new VendorId(bb);
        guidPrefix = new GuidPrefix(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(0);
        protocolVersion.writeTo(bb);
        vendorId.writeTo(bb);
        guidPrefix.writeTo(bb);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + guidPrefix;
    }
}
