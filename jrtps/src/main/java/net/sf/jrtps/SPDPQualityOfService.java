package net.sf.jrtps;

import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;

public class SPDPQualityOfService extends QualityOfService {
	public SPDPQualityOfService() {
		super(); // create defaults
		
		try {
			setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT_LOCAL));
			setPolicy(new QosReliability(QosReliability.Kind.BEST_EFFORT, new Duration(0)));
		} catch (InconsistentPolicy e) {
			throw new RuntimeException("Internal error", e);
		}
	}
}
