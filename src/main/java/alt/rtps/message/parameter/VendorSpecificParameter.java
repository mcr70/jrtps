package alt.rtps.message.parameter;

public class VendorSpecificParameter extends Parameter {
	private short vendorParamId;

	VendorSpecificParameter(short paramId) {
		super(ParameterEnum.PID_VENDOR_SPECIFIC);
		
		this.vendorParamId = paramId;
	}
	
	public int getVendorId() {
		return vendorParamId;
	}
}