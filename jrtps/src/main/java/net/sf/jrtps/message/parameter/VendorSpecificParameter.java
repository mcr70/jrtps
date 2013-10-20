package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class VendorSpecificParameter extends Parameter {
	private short vendorParamId;

	VendorSpecificParameter(short paramId) {
		super(ParameterEnum.PID_VENDOR_SPECIFIC);
		
		this.vendorParamId = paramId;
	}
	
	public short getVendorParameterId() {
		return vendorParamId;
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
	}

	public String toString() {
		return super.toString() + ": " + getVendorParameterId() + " (" + Arrays.toString(getBytes()) + ")";
	}
}