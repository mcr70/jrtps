package alt.rtps.message.parameter;


public class QosOwnershipStrength extends Parameter implements QualityOfService {
	QosOwnershipStrength() {
		super(ParameterEnum.PID_OWNERSHIP_STRENGTH);
	}
}