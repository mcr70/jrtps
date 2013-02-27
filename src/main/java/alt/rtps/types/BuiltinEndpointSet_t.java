package alt.rtps.types;

public class BuiltinEndpointSet_t {
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER = new BuiltinEndpointSet_t(0x00000001);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR = new BuiltinEndpointSet_t(0x00000001 << 1);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER = new BuiltinEndpointSet_t(0x00000001 << 2);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR = new BuiltinEndpointSet_t(0x00000001 << 3);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER = new BuiltinEndpointSet_t(0x00000001 << 4);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR = new BuiltinEndpointSet_t(0x00000001 << 5);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_ANNOUNCER = new BuiltinEndpointSet_t(0x00000001 << 6);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_DETECTOR = new BuiltinEndpointSet_t(0x00000001 << 7);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_ANNOUNCER = new BuiltinEndpointSet_t(0x00000001 << 8);
	public static BuiltinEndpointSet_t DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_DETECTOR = new BuiltinEndpointSet_t(0x00000001 << 9);
	public static BuiltinEndpointSet_t BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER = new BuiltinEndpointSet_t(0x00000001 << 10);
	public static BuiltinEndpointSet_t BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER = new BuiltinEndpointSet_t(0x00000001 << 11);
	
	private int value;
	
	private BuiltinEndpointSet_t(int value) {
		this.value = value;
	}
}
