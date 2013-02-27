package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Duration_t;


public class QosReliability extends Parameter implements QualityOfService {
	private int kind;
	private Duration_t max_blocking_time;
	
	public enum Kind {
		BEST_EFFORT(0), RELIABLE(1), UNKNOWN_RELIABILITY_KIND(99);

		private int __kind;
		private Kind(int kind) {
			__kind = kind;
		}
	}

	QosReliability() {
		super(ParameterEnum.PID_RELIABILITY);
	}


	public Duration_t getMaxBlockingTime() {
		return max_blocking_time;
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.BEST_EFFORT;
		case 1: return Kind.RELIABLE; 
		}

		// TODO: OSPL 5.5 uses KIND=2, maybe there is an offset error like 1 for BEST_EFFORT and 2 for RELIABLE
		return Kind.UNKNOWN_RELIABILITY_KIND;
	}


	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.kind = bb.read_long();
		max_blocking_time = new Duration_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
		max_blocking_time.writeTo(buffer);
	}

	public String toString() {	
		return super.toString() + "(" + getKind()  + "[" + kind + "]," + max_blocking_time + ")";
	}
}