package net.sf.jrtps.types;

/**
 * This class holds contains for BuiltinEndpointSet.
 * 
 * @author mcr70
 *
 */
public final class BuiltinEndpointSet {
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER = 0x00000001 << 0;
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR = 0x00000001 << 1;
	public static final int DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER = 0x00000001 << 2;
	public static final int DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR = 0x00000001 << 3;
	public static final int DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER = 0x00000001 << 4;
	public static final int DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR = 0x00000001 << 5;
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_ANNOUNCER = 0x00000001 << 6;
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_DETECTOR = 0x00000001 << 7;
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_ANNOUNCER = 0x00000001 << 8;
	public static final int DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_DETECTOR = 0x00000001 << 9;
	public static final int BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER = 0x00000001 << 10;
	public static final int BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER = 0x00000001 << 11;

	private BuiltinEndpointSet() { 
	}
}
