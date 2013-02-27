package alt.rtps.message.parameter;


public class QosLifespan extends Parameter implements QualityOfService {
	QosLifespan() {
		super(ParameterEnum.PID_LIFESPAN);
	}
}