package alt.rtps.types;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * Every Participant has GUID <prefix, ENTITYID_PARTICIPANT>, where the constant ENTITYID_PARTICIPANT is a
 * special value defined by the RTPS protocol. Its actual value depends on the PSM.
 * The implementation is free to choose the prefix, as long as every Participant in the Domain has a unique GUID.
 * <p>
 * This implementation has chosen to use prefix: domainId, participantId, 0, 0, 0xcafebabe
 * <p>
 * 
 * @author mcr70
 * @see 8.2.4.2 The GUIDs of RTPS Participants
 * @see 9.3.1.1 Mapping of the GuidPrefix_t
 */
public class GuidPrefix_t {
	public static final int LENGTH = 12;

	public static final GuidPrefix_t GUIDPREFIX_UNKNOWN = new GuidPrefix_t(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0});

	/**
	 * bytes must be of length 12
	 */
	private final byte[] bytes;

	public GuidPrefix_t(byte domainId, byte participantid) {
		this(new byte[] {domainId, participantid, 0, 0, 0xc,0xa,0xf,0xe,0xb,0xa,0xb,0xe});
	}

	public GuidPrefix_t(RTPSByteBuffer bb) {
		bytes = new byte[12];
		bb.read(bytes);
	}

	private GuidPrefix_t(byte[] bytes) {
		this.bytes = bytes;

		assert bytes.length == 12;
	}

	/**
	 * Get the domainId associated with this GuidPrefix
	 */
	public byte getDomainId() {
		return bytes[0];
	}

	/**
	 * Get the participantId associated with this GuidPrefix
	 */
	public byte getParticipantId() {
		return bytes[1];
	}

	
	public byte[] getBytes() {
		return bytes;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("GuidPrefix[");
		for (int i = 0; i < bytes.length - 1; i++) {
			sb.append(bytes[i]);
			sb.append(',');
		}
		sb.append(bytes[bytes.length-1]);
		sb.append(']');

		return sb.toString();
	}



	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(bytes);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GuidPrefix_t) {
			GuidPrefix_t other = (GuidPrefix_t) o;

			return Arrays.equals(bytes, other.bytes);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}
}
