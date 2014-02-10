package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in
 * Topic Data
 * 
 * @author mcr70
 * 
 */
public class VendorId_t {
    public static final int LENGTH = 2;

    public static final VendorId_t VENDORID_INVALID = new VendorId_t(new byte[] { 0, 0 });
    public static final VendorId_t VENDORID_JRTPS = new VendorId_t(new byte[] { (byte) 0xca, (byte) 0xfe });
    public static final VendorId_t PRISMTECH = new VendorId_t(new byte[] { (byte) 0x1, (byte) 0x2 });

    private final byte[] bytes;

    public VendorId_t(RTPSByteBuffer bb) {
        this.bytes = new byte[2];
        bb.read(bytes);
    }

    public VendorId_t(byte[] bytes) {
        this.bytes = bytes;

        assert bytes.length == 2;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("vendor ");
        sb.append(bytes[0]);
        sb.append(',');
        sb.append(bytes[1]);

        if (bytes[0] == 1) {
            sb.append('(');

            // See http://portals.omg.org/dds/content/page/dds-rtps-vendor-ids
            switch (bytes[1]) {
            case 1:
                sb.append("Real-Time Innovations, Inc. (RTI)");
                break;
            case 2:
                sb.append("PrismTech");
                break;
            case 3:
                sb.append("Object Computing Incorporated, Inc. (OCI)");
                break;
            case 4:
                sb.append("MilSoft");
                break;
            case 5:
                sb.append("Kongsberg");
                break;
            case 6:
                sb.append("TwinOaks Computing, Inc.");
                break;
            case 7:
                sb.append("Lakota Technical Solutions, Inc.");
                break;
            case 8:
                sb.append("ICOUP Consulting");
                break;
            case 9:
                sb.append("ETRI Electronics and Telecommunication Research Institute");
                break;
            }
            sb.append(')');
        } else if (bytes[0] == (byte) 0xca && bytes[1] == (byte) 0xfe) {
            sb.append("(jRTPS)");
        }

        return sb.toString();
    }

    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write(bytes);
    }
}
