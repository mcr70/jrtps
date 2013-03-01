package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class TopicName extends Parameter {
	private String name;

	public TopicName(String name) {
		super(ParameterEnum.PID_TOPIC_NAME);
		this.name = name;

	}

	TopicName() {
		super(ParameterEnum.PID_TOPIC_NAME);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}


	public String getName() {
		if (name == null) {
			byte[] bytes = getBytes();

			// TODO: Hardcoded endianess of Strings in CDR InputStream
			RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
			bb.setEndianess(true); 
//			int sSize = bb.read_long();
//			name = new String(bytes, 4, sSize -1);

			name = bb.read_string();
		}
		
		return name; // TODO, @see table 9.14: string<256> vs. rtps_rcps.idl: string			
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_string(name);
	}
	
	public String toString() {
		return super.toString() + "(" + getName() + ")";
	}
}