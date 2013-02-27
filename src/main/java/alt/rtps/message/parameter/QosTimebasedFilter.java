package alt.rtps.message.parameter;


public class QosTimebasedFilter extends Parameter implements QualityOfService {
	QosTimebasedFilter() {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
	}
}