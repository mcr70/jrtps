package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class TypeName extends Parameter {
	private String typeName;

	TypeName() {
		super(ParameterEnum.PID_TYPE_NAME);
	}
	
	public TypeName(String typeName) {
		super(ParameterEnum.PID_TYPE_NAME);
		this.typeName = typeName;
	}
	
	public String getTypeName() {
		if (typeName == null) {
			byte[] bytes = getBytes();

			// TODO: Hardcoded endianess of Strings in CDR InputStream
			RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
			bb.setEndianess(true); 
//			int sSize = bb.read_long();
//			typeName = new String(bytes, 4, sSize -1);
			
			typeName = bb.read_string();
		}
		
		return typeName; // TODO, @see table 9.14: string<256> vs. rtps_rcps.idl: string
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_string(getTypeName());
	}
	
	public String toString() {
		return super.toString() + "(" + getTypeName() + ")";
	}
}