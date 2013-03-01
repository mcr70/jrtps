package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;

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

	public String toString() {
		return super.toString() + ": " + getVendorParameterId() + " (" + Arrays.toString(getBytes()) + ")";
	}
}