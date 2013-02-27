package alt.rtps.message.parameter;


public class QosResourceLimits extends Parameter implements QualityOfService {
	QosResourceLimits() {
		super(ParameterEnum.PID_RESOURCE_LIMITS);
	}
}