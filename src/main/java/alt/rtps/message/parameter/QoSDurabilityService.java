package alt.rtps.message.parameter;


public class QoSDurabilityService extends Parameter implements QualityOfService {
	QoSDurabilityService() {
		super(ParameterEnum.PID_DURABILITY_SERVICE);
	}
}