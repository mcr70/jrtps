package alt.rtps.message.parameter;


public class ParticipantBuiltinEndpoints extends EndpointSet {
	public ParticipantBuiltinEndpoints(int endpoints) {
		super(ParameterEnum.PID_PARTICIPANT_BUILTIN_ENDPOINTS, endpoints);
	}

	ParticipantBuiltinEndpoints() {
		super(ParameterEnum.PID_PARTICIPANT_BUILTIN_ENDPOINTS, 0);
	}
}