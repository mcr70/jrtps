package alt.rtps.message.parameter;

import java.util.Arrays;


public class QosPartition extends Parameter implements QualityOfService {
	QosPartition() {
		super(ParameterEnum.PID_PARTITION);
	}
	
	public String toString() {
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}
}