package alt.rtps.message.parameter;


public class QosHistory extends Parameter implements QualityOfService {
	QosHistory() {
		super(ParameterEnum.PID_HISTORY);
	}
}