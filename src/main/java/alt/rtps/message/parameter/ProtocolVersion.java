package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.ProtocolVersion_t;

public class ProtocolVersion extends Parameter {
	private ProtocolVersion_t version;

	public ProtocolVersion(ProtocolVersion_t version) {
		this();
		this.version = version;
	}
	
	ProtocolVersion() {
		super(ParameterEnum.PID_PROTOCOL_VERSION);
	}
	
	public ProtocolVersion_t getProtocolVersion() {
		return version;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.version = new ProtocolVersion_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		version.writeTo(buffer);
	}
	
	public String toString() {
		return super.toString() + ": " + getProtocolVersion();
	}
}