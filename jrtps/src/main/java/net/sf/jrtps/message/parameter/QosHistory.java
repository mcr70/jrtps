package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * QosHistory.
 * This policy must be consistent with QosResourceLimits, so that HISTORY.depth <= RESOURCE_LIMITS.max_samples_per_instance
 * 
 * @author mcr70
 *
 */
public class QosHistory extends Parameter implements DataReaderPolicy, TopicPolicy, DataWriterPolicy {
	private int kind;
	private int depth;
	
	public enum Kind {
		KEEP_LAST, KEEP_ALL;
	}
	
	QosHistory() {
		super(ParameterEnum.PID_HISTORY);
	}

	public QosHistory(Kind kind, int depth) {
		super(ParameterEnum.PID_HISTORY);
		
		switch(kind) {
		case KEEP_LAST: this.kind = 0; break;
		case KEEP_ALL: this.kind = 1; break;
		}
		
		this.depth = depth;
	}
	
	/**
	 * Get the depth of this QosHistory. Depth 1 means that only latest sample is kept.

	 * @return depth
	 */
	public int getDepth() {
		return depth;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.kind = bb.read_long();
		this.depth = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(kind);
		bb.write_long(depth);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true. TODO: check this
	}

	/**
	 * Get the default QosHistory: KEEP_LAST, 1
	 * 
	 * @return default QosHistory
	 */
	public static QosHistory defaultHistory() {
		return new QosHistory(Kind.KEEP_LAST, 1);
	}
}