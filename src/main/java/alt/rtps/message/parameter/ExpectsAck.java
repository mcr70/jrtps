package alt.rtps.message.parameter;


@Deprecated
public class ExpectsAck extends Parameter {
	ExpectsAck() {
		super(ParameterEnum.PID_EXPECTS_ACK);
	}
}