package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * VendorId
 * 
 * @author mcr70
 */
public class VendorId extends Parameter {
    public static final VendorId VENDORID_INVALID = new VendorId(new byte[] { 0x00, 0x00 });
    public static final VendorId VENDORID_SECURED = new VendorId(new byte[] { 0x00, 0x01 }); // 8.5.1.9.4 Operation: encode_rtps_message
    public static final VendorId VENDORID_JRTPS = new VendorId(new byte[] { (byte) 0xca, (byte) 0xfe });
    public static final VendorId PRISMTECH = new VendorId(new byte[] { (byte) 0x1, (byte) 0x2 });

	private byte[] bytes;

    private VendorId(byte[] bytes) {
        super(ParameterId.PID_VENDORID);
        this.bytes = bytes;
    }

    public VendorId(RTPSByteBuffer bb) {
        super(ParameterId.PID_VENDORID);
        this.bytes = new byte[2];
        bb.read(bytes);
    }


    VendorId() {
        super(ParameterId.PID_VENDORID);
    }


    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.bytes = new byte[2];
        bb.read(bytes);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write(bytes);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(":");
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
            default:
                sb.append("unknown");
            }
            sb.append(')');
        } else if (bytes[0] == (byte) 0xca && bytes[1] == (byte) 0xfe) {
            sb.append("(jRTPS)");
        }

        return sb.toString();
    }
}