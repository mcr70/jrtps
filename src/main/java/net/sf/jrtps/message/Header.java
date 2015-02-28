package net.sf.jrtps.message;

import java.util.Arrays;

import net.sf.jrtps.message.parameter.ProtocolVersion;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

/**
 * The Header identifies the message as belonging to the RTPS protocol. The
 * Header identifies the version of the protocol and the vendor that sent the
 * message.
 * 
 * @author mcr70
 * 
 */
public class Header {
    private static final byte[] HDR_START = new byte[] { 'R', 'T', 'P', 'S' };
    // private ProtocolId_t protocol;
    private byte[] hdrStart;
    private ProtocolVersion version;
    private VendorId vendorId;
    private GuidPrefix guidPrefix;

    /**
     * Constructor for Header. ProtocolVersion is set to 2.1 and VendorId is set
     * to jRTPS.
     * 
     * @param prefix
     */
    public Header(GuidPrefix prefix) {
    	this(prefix, ProtocolVersion.PROTOCOLVERSION_2_1, VendorId.JRTPS);
    }

    /**
     * Constructor with given values.
     * @param prefix GuidPrefix 
     * @param version Version of the RTPS protocol
     * @param vendorId VendorId
     */
    public Header(GuidPrefix prefix, ProtocolVersion version, VendorId vendorId) {
		this.hdrStart = HDR_START;
    	this.guidPrefix = prefix;
		this.version = version;
		this.vendorId = vendorId;
    }
    
    /**
     * Constructs Header from given RTPSByteBuffer.
     * 
     * @param bb
     * @throws IllegalMessageException 
     */
    Header(RTPSByteBuffer bb) throws IllegalMessageException {
    	if (bb.getBuffer().remaining() < 20) {
    		throw new IllegalMessageException("Message length must be at least 20 bytes, was " + 
    				Arrays.toString(bb.getBuffer().array()));
    	}
    	
        hdrStart = new byte[4];
        bb.read(hdrStart);
        if (!Arrays.equals(HDR_START, hdrStart)) {
        	throw new IllegalMessageException("Illegal message header start bytes: " + 
        			Arrays.toString(hdrStart) + ", expected " + Arrays.toString(HDR_START));
        }
        
        version = new ProtocolVersion(bb);
        vendorId = new VendorId(bb);
        guidPrefix = new GuidPrefix(bb);
    }

    /**
     * Writer this Header to given RTPSByteBuffer.
     * 
     * @param bb
     */
    public void writeTo(RTPSByteBuffer bb) {
        bb.write(hdrStart);
        version.writeTo(bb);
        vendorId.writeTo(bb);
        guidPrefix.writeTo(bb);
    }

    /**
     * Defines a default prefix to use for all GUIDs that appear within the
     * Submessages contained in the message.
     */
    public GuidPrefix getGuidPrefix() {
        return guidPrefix;
    }

    /**
     * Indicates the vendor that provides the implementation of the RTPS
     * protocol.
     */
    public VendorId getVendorId() {
        return vendorId;
    }

    /**
     * Identifies the version of the RTPS protocol.
     */
    public ProtocolVersion getVersion() {
        return version;
    }
	
	
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(new String(hdrStart));
        sb.append(", ");
        sb.append(version.toString());
        sb.append(", ");
        sb.append(vendorId.toString());
        sb.append(", ");
        sb.append(guidPrefix.toString());

        return sb.toString();
    }

}
