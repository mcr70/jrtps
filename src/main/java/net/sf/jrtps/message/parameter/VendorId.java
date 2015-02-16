package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * VendorIds are are assigned by Object Management Group.
 * See http://portals.omg.org/dds/content/page/dds-rtps-vendor-and-product-ids
 * for full list of assigned ids.
 * 
 * @author mcr70
 */
public class VendorId extends Parameter {
	public static final VendorId VENDORID_INVALID = new VendorId(new byte[] { 0x00, 0x00 });
	public static final VendorId VENDORID_SECURED = new VendorId(new byte[] { 0x00, 0x01 }); // 8.5.1.9.4 Operation: encode_rtps_message

	public static final VendorId CONNEXT_DDS = new VendorId(new byte[] { (byte) 0x01, (byte) 0x01 });
	public static final VendorId OPENSPLICE = new VendorId(new byte[] { (byte) 0x01, (byte) 0x02 });
	public static final VendorId OPENDDS = new VendorId(new byte[] { (byte) 0x01, (byte) 0x03 });
	public static final VendorId MILSOFT = new VendorId(new byte[] { (byte) 0x01, (byte) 0x04 });
	public static final VendorId INTERCOM_DDS = new VendorId(new byte[] { (byte) 0x01, (byte) 0x05 });
	public static final VendorId COREDX_DDS = new VendorId(new byte[] { (byte) 0x01, (byte) 0x06 });
	public static final VendorId LAKOTA = new VendorId(new byte[] { (byte) 0x01, (byte) 0x07 });
	public static final VendorId ICOUP = new VendorId(new byte[] { (byte) 0x01, (byte) 0x08 });
	public static final VendorId ETRI = new VendorId(new byte[] { (byte) 0x01, (byte) 0x09 });
	public static final VendorId CONNEXT_DDS_MICRO = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0a });
	public static final VendorId VORTEX_CAFE = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0b });
	public static final VendorId VORTEX_GATEWAY = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0c });
	public static final VendorId VORTEX_LITE = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0d });
	public static final VendorId QEO = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0e });
	public static final VendorId EPROSIMA = new VendorId(new byte[] { (byte) 0x01, (byte) 0x0f });
	public static final VendorId VORTEX_CLOUD = new VendorId(new byte[] { (byte) 0x01, (byte) 0x20 });

	public static final VendorId JRTPS = new VendorId(new byte[] { (byte) 0x01, (byte) 0x21 });

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

		int id = (bytes[0] << 8) | bytes[1];
		sb.append('(');

		switch (id) {
		case 1:
			sb.append("Real-Time Innovations, Inc. (RTI) - Connext DDS");
			break;
		case 2:
			sb.append("PrismTech - OpenSplice");
			break;
		case 3:
			sb.append("Object Computing Incorporated, Inc. (OCI) - OpenDDS");
			break;
		case 4:
			sb.append("MilSoft");
			break;
		case 5:
			sb.append("Gallium Visual Systems Inc. - InterCOM DDS");
			break;
		case 6:
			sb.append("TwinOaks Computing, Inc. - CoreDX DDS");
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
		case 0xa:
			sb.append("Real-Time Innovations, Inc. (RTI) - Connext DDS Micro");
			break;
		case 0xb:
			sb.append("PrimsTech - Vortex Cafe");
			break;
		case 0xc:
			sb.append("PrimsTech - Vortex Gateway");
			break;
		case 0xd:
			sb.append("PrimsTech - Vortex Lite");
			break;
		case 0xe:
			sb.append("Technicolor Inc. - Qeo");
			break;
		case 0xf:
			sb.append("eProsima - Fast RTPS");
			break;
		case 0x20:
			sb.append("PrismTech - Vortex Cloud");
			break;
		case 0x21:
			sb.append("jRTPS");
			break;
		default:
			sb.append("unknown");
		}

		sb.append(')');

		return sb.toString();
	}
}