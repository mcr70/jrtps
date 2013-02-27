package alt.rtps.message.parameter;


public class QosLatencyBudget extends Parameter implements QualityOfService {
	QosLatencyBudget() {
		super(ParameterEnum.PID_LATENCY_BUDGET);
	}
}