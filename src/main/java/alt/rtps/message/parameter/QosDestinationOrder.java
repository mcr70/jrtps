package alt.rtps.message.parameter;


public class QosDestinationOrder extends Parameter implements QualityOfService {
	QosDestinationOrder() {
		super(ParameterEnum.PID_DESTINATION_ORDER);
	}
}