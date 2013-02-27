package alt.rtps.message.parameter;


public class QosDeadline extends Parameter implements QualityOfService {
	QosDeadline() {
		super(ParameterEnum.PID_DEADLINE);
	}
}