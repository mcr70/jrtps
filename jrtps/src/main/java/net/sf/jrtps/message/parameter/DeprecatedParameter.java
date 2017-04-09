package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Deprecated parameters are defined inside this class.
 * Each deprecated parameter is read correctly by jRTPS, but they
 * are not interpreted in any way.
 * 
 * @author mcr70
 */
public class DeprecatedParameter extends Parameter {
	protected DeprecatedParameter(ParameterId id) {
		super(id);
	}

	@Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }		

	public static class ExpectsAck extends DeprecatedParameter {
	    ExpectsAck() {
	        super(ParameterId.PID_EXPECTS_ACK);
	    }
	}

	// Depreacted parameters follow
	
	public static class ManagerKey extends DeprecatedParameter {
	    ManagerKey() {
	        super(ParameterId.PID_MANAGER_KEY);
	    }
	}		
	public static class Persistence extends DeprecatedParameter {
	    Persistence() {
	        super(ParameterId.PID_PERSISTENCE);
	    }
	}	
	public static class RecvQueueSize extends DeprecatedParameter {
	    RecvQueueSize() {
	        super(ParameterId.PID_RECV_QUEUE_SIZE);
	    }
	}	
	public static class ReliabilityEnabled extends DeprecatedParameter {
	    ReliabilityEnabled() {
	        super(ParameterId.PID_RELIABILITY_ENABLED);
	    }
	}
	public static class ReliabilityOffered extends DeprecatedParameter {
	    ReliabilityOffered() {
	        super(ParameterId.PID_RELIABILITY_OFFERED);
	    }
	}	
	public static class SendQueueSize extends DeprecatedParameter {
	    SendQueueSize() {
	        super(ParameterId.PID_SEND_QUEUE_SIZE);
	    }
	}
	public static class Type2Checksum extends DeprecatedParameter {
	    Type2Checksum() {
	        super(ParameterId.PID_TYPE2_CHECKSUM);
	    }
	}
	public static class Type2Name extends DeprecatedParameter {
	    Type2Name() {
	        super(ParameterId.PID_TYPE2_NAME);
	    }
	}	
	public static class TypeChecksum extends DeprecatedParameter {
	    TypeChecksum() {
	        super(ParameterId.PID_TYPE_CHECKSUM);
	    }	
	}
	public static class VargappsSequenceNumberLast extends DeprecatedParameter {
	    VargappsSequenceNumberLast() {
	        super(ParameterId.PID_VARGAPPS_SEQUENCE_NUMBER_LAST);
	    }
	}
}
