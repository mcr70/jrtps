package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;


public class QosPresentation extends Parameter implements QualityOfService {
	public enum Kind {
		INSTANCE, TOPIC, GROUP, ILLEGAL;
				
		public boolean isCompatible(Kind requested) {
			return ordinal() >= requested.ordinal();
		}
	};
	
	private Kind kind;
	private int access_scope;
	private boolean coherent_access;
	private boolean ordered_access;

	QosPresentation() {
		super(ParameterEnum.PID_PRESENTATION);
	}
	
	QosPresentation(Kind kind, boolean coherent_access, boolean ordered_access) {
		super(ParameterEnum.PID_PRESENTATION);
		this.kind = kind;
		this.access_scope = kind.ordinal();
		this.coherent_access = coherent_access;
		this.ordered_access = ordered_access;
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.access_scope = bb.read_long();
		this.coherent_access = bb.read_boolean();
		this.ordered_access = bb.read_boolean();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(access_scope);
		bb.write_boolean(coherent_access);
		bb.write_boolean(ordered_access);
	}

	public Kind getKind() {
		if (kind == null) {
			switch (access_scope) {
			case 0: kind = Kind.INSTANCE; break;
			case 1: kind = Kind.TOPIC; break;
			case 2: kind = Kind.GROUP; break;
			default: 
				kind = Kind.ILLEGAL;
			}
		}
		
		return kind;
	}
	
	public String toString() {
		return super.toString() + "(" + getKind() + ", coherent=" + coherent_access + ", ordered=" + ordered_access + ")";
	}
}