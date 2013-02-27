package alt.rtps.message.parameter;


public class QosTransportPriority extends Parameter implements QualityOfService {
	QosTransportPriority() {
		super(ParameterEnum.PID_TRANSPORT_PRIORITY);
	}
}