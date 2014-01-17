package net.sf.jrtps;

import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosPresentation;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.types.Duration;

/**
 * Represents QuliatyOfService used with SEDP.
 * See ch. 7.1.5 Built-in Topics in DDS v1.2 specification
 * 
 * @author mcr70
 */
public class SEDPQualityOfService extends QualityOfService {
	public SEDPQualityOfService() {
		super(); // Creates defaults
		
		try {
			setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT)); // TODO: OSPL uses TRANSIENT, while TRANSIENT_LOCAL would be correct
			setPolicy(new QosPresentation(QosPresentation.Kind.TOPIC, false, false));
			setPolicy(new QosDeadline(Duration.INFINITE));
			setPolicy(new QosOwnership(QosOwnership.Kind.SHARED));
			setPolicy(new QosLiveliness(QosLiveliness.Kind.AUTOMATIC, new Duration(0,0)));
			setPolicy(new QosTimeBasedFilter(new Duration(0)));
			setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(100)));
			setPolicy(new QosDestinationOrder(QosDestinationOrder.Kind.BY_RECEPTION_TIMESTAMP));
			setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));
			setPolicy(new QosResourceLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		} catch (InconsistentPolicy e) {
			throw new RuntimeException("Internal error", e);
		}
	}
}
