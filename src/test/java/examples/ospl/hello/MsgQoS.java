package examples.ospl.hello;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosReliability.Kind;
import net.sf.jrtps.types.Duration;

public class MsgQoS extends QualityOfService {
    public MsgQoS() {
    	try {
			setPolicy(new QosReliability(Kind.RELIABLE, new Duration(10000)));
		} catch (InconsistentPolicy e) {
			throw new RuntimeException("Internal error", e);
		}
    }
}
