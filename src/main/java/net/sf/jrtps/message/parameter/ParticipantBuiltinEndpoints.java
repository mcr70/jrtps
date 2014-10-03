package net.sf.jrtps.message.parameter;

public class ParticipantBuiltinEndpoints extends EndpointSet {
    public ParticipantBuiltinEndpoints(int endpoints) {
        super(ParameterId.PID_PARTICIPANT_BUILTIN_ENDPOINTS, endpoints);
    }

    ParticipantBuiltinEndpoints() {
        super(ParameterId.PID_PARTICIPANT_BUILTIN_ENDPOINTS, 0);
    }
}