package net.sf.jrtps.builtin;

import java.util.Arrays;

import net.sf.jrtps.types.GuidPrefix_t;

/**
 * ParticipantMessage is used to implement liveliness protocol.
 * 
 * @author mcr70
 */
public class ParticipantMessage {
	public static final String BUILTIN_TOPIC_NAME = "DCPSParticipantMessage";
	public static final byte[] AUTOMATIC_LIVELINESS_KIND = new byte[] {0, 0, 0, 1};
	public static final byte[] MANUAL_LIVELINESS_KIND = new byte[] {0, 0, 0, 2};
	
	private final GuidPrefix_t prefix;
	private final byte[] data;
	private final byte[] kind;
	
	public ParticipantMessage(GuidPrefix_t prefix, byte[] kind, byte[] data) {
		this.prefix = prefix;
		this.kind = kind;
		this.data = data;
	}
	
	public GuidPrefix_t getGuidPrefix() {
		return prefix;
	}
	
	public byte[] getKind() {
		return kind;
	}
	
	public byte[] getData() {
		return data;
	}

	public boolean isAutomaticLivelinessKind() {
		return Arrays.equals(AUTOMATIC_LIVELINESS_KIND, kind);
	}

	public boolean isManualLivelinessKind() {
		return Arrays.equals(MANUAL_LIVELINESS_KIND, kind);
	}
}
