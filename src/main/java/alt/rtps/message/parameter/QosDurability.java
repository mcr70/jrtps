package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosDurability extends Parameter implements QualityOfService {
	private int kind;

	public enum Kind {
		VOLATILE(0), TRANSIENT_LOCAL(1), TRANSIENT(2), PERSISTENT(3), UNKNOWN_DURABILITY_KIND(99);

		private int __kind;
		private Kind(int kind) {
			__kind = kind;
		}
	}
	
	QosDurability() {
		super(ParameterEnum.PID_DURABILITY);
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.kind = bb.read_long();
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.VOLATILE;
		case 1: return Kind.TRANSIENT_LOCAL;
		case 2: return Kind.TRANSIENT;
		case 3: return Kind.PERSISTENT;
		}
		
		return null;
	}
	
	public String toString() {
		return super.toString() + "(" + getKind() + ")";
	}
}