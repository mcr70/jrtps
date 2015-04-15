package examples.ospl.hello;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosGroupData;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLatencyBudget;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosPartition;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosReliability.Kind;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.message.parameter.QosUserData;
import net.sf.jrtps.types.Duration;

public class MsgQoS extends QualityOfService {
    public MsgQoS() {
    	try {
// [QosLatencyBudget([0:0]), QosResourceLimits(max_instances -1, max_samples -1, max_samples_per_instance -1), 
//  QosReliability(RELIABLE, [0:100000000]), QosDurability(TRANSIENT), QosTimeBasedFilter([0:0]), 
//  QosLiveliness(AUTOMATIC, [2147483647:-1]), QosUserData[0, 0, 0, 0], QosPartition([]), QosOwnership(SHARED), 
//  QosDeadline([2147483647:-1]), QosGroupData[0, 0, 0, 0], QosDestinationOrder(BY_RECEPTION_TIMESTAMP), 
//  QosHistory(KEEP_LAST, 1)]
    		setPolicy(new QosLatencyBudget(new Duration(0,0)));
    		setPolicy(new QosResourceLimits(-1, -1, -1));
    		
    		setPolicy(new QosReliability(Kind.RELIABLE, new Duration(10000)));
    		setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT));
    		setPolicy(new QosTimeBasedFilter(new Duration(0, 0)));
    		
    		setPolicy(new QosLiveliness(QosLiveliness.Kind.AUTOMATIC, new Duration(Integer.MAX_VALUE, -1)));
    		setPolicy(new QosUserData(new byte[] {0,0,0,0}));
    		setPolicy(new QosPartition(new String[]{"HelloWorld example"}));
    		setPolicy(new QosOwnership(QosOwnership.Kind.SHARED));
    		
    		setPolicy(new QosDeadline(new Duration(Integer.MAX_VALUE, -1)));
    		setPolicy(new QosGroupData(new byte[] {0,0,0,0}));
    		setPolicy(new QosDestinationOrder(QosDestinationOrder.Kind.BY_RECEPTION_TIMESTAMP));
    		setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));
		} catch (InconsistentPolicy e) {
			throw new RuntimeException("Internal error", e);
		}
    }
}
