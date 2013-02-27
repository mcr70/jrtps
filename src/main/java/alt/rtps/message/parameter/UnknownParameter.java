package alt.rtps.message.parameter;

import java.util.Arrays;

public class UnknownParameter extends Parameter {
	private final short paramId;

	protected UnknownParameter(short paramId) {
		super(ParameterEnum.PID_UNKNOWN_PARAMETER);

		this.paramId = paramId;
	}

	
	public String toString() {
		return super.toString() + ", ID " + paramId + ": " + Arrays.toString(getBytes());
	}
}
