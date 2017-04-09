package net.sf.jrtps.aperf1;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosLatencyBudget;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosReliability.Kind;
import net.sf.jrtps.types.Duration;

public class PQoS extends QualityOfService {
    public PQoS() {
    	try {
    		// QosPresentation(INSTANCE, coherent=false, ordered=false), QosDurability(VOLATILE), 
    		// QosDurabilityService, QosLiveliness(AUTOMATIC, [2147483647:-1]), 
    		// QosOwnership(SHARED), QosReliability(RELIABLE, [0:429496729]), 
    		// QosDestinationOrder(BY_RECEPTION_TIMESTAMP), 
    		// QosLifespan([2147483647:-1]), QosLatencyBudget([0:0]), 
    		// QosDeadline([2147483647:-1]), QosOwnershipStrength(0)]
    		setPolicy(new QosLatencyBudget(new Duration(0,0)));    		
    		setPolicy(new QosReliability(Kind.RELIABLE, new Duration(10000)));
    		setPolicy(new QosDurability(QosDurability.Kind.VOLATILE));
    		
    		setPolicy(new QosLiveliness(QosLiveliness.Kind.AUTOMATIC, new Duration(Integer.MAX_VALUE, -1)));
    		setPolicy(new QosOwnership(QosOwnership.Kind.SHARED));
    		
    		setPolicy(new QosDeadline(new Duration(Integer.MAX_VALUE, -1)));
    		setPolicy(new QosDestinationOrder(QosDestinationOrder.Kind.BY_RECEPTION_TIMESTAMP));
		} catch (InconsistentPolicy e) {
			throw new RuntimeException("Internal error", e);
		}
    }
}
