package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This QosPolicy determines how Readers form the timestamp of the samples.<p>
 * 
 * See DDS specification v1.2, ch. 7.1.3.17 
 *  
 * @author mcr70
 *
 */
public class QosDestinationOrder extends Parameter implements QosPolicy {
	private int kind;
	
	public enum Kind {
		BY_RECEPTION_TIMESTAMP, BY_SOURCE_TIMESTAMP, ILLEGAL_KIND
	}
	
	QosDestinationOrder() {
		super(ParameterEnum.PID_DESTINATION_ORDER);
	}

	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.BY_RECEPTION_TIMESTAMP;
		case 1: return Kind.BY_SOURCE_TIMESTAMP;
		}
		
		return Kind.ILLEGAL_KIND;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.kind = bb.read_long();
		if (kind != 0 && kind != 1) {
			kind = -1; // for isCompatible(): never compatible
		}
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(kind);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosDestinationOrder) {
			QosDestinationOrder qOther = (QosDestinationOrder) other;
			
			return kind >= qOther.kind;
		}
		
		return false;
	}
}