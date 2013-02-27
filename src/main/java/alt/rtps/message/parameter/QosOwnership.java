package alt.rtps.message.parameter;


public class QosOwnership extends Parameter implements QualityOfService {
	QosOwnership() {
		super(ParameterEnum.PID_OWNERSHIP);
	}
}