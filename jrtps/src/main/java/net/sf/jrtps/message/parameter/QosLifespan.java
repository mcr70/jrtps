package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;

/**
 * See DDS specification v1.2, ch 7.1.3.16
 * 
 * @author mcr70
 *
 */
public class QosLifespan extends Parameter implements DataReaderPolicy, DataWriterPolicy, TopicPolicy, InlineParameter {
	private Duration_t duration;
	
	QosLifespan() {
		super(ParameterEnum.PID_LIFESPAN);
	}

	/**
	 * Constructor for QosLifespan.
	 * @param duration
	 */
	public QosLifespan(Duration_t duration) {
		super(ParameterEnum.PID_LIFESPAN);
		this.duration = duration;
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.duration = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		duration.writeTo(bb);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true. TODO: check this
	}

	/**
	 * Get the default QosLifespan with infinite duration.
	 * 
	 * @return default QosLifespan
	 */
	public static QosLifespan defaultLifespan() {
		return new QosLifespan(Duration_t.INFINITE);
	}
}