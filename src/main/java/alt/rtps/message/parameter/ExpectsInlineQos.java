package alt.rtps.message.parameter;


public class ExpectsInlineQos extends Parameter {
	ExpectsInlineQos() {
		super(ParameterEnum.PID_EXPECTS_INLINE_QOS);
	}
	
	public boolean expectsInlineQos() {
		return getBytes()[0] == 1; // TODO: Check boolean encoding
	}
}