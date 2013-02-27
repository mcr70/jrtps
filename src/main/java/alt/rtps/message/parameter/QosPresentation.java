package alt.rtps.message.parameter;

import java.util.Arrays;


public class QosPresentation extends Parameter implements QualityOfService {
	QosPresentation() {
		super(ParameterEnum.PID_PRESENTATION);
	}
	
	public String toString() {
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}
}