package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.VendorId_t;

public class VendorId extends Parameter {
	private VendorId_t vendorId;

	public VendorId(VendorId_t vendorid) {
		this();
		vendorId = vendorid;
	}

	VendorId() {
		super(ParameterEnum.PID_VENDORID);
	}
	

	public VendorId_t getVendorId() {
		return vendorId;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.vendorId = new VendorId_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		vendorId.writeTo(buffer);
	}

	
	public String toString() {
		return super.toString() + ": " + getVendorId();
	}
}