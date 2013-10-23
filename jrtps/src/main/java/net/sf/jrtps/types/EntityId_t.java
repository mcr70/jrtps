package net.sf.jrtps.types;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * 
 * @author mcr70
 * @see 9.3.1.2 Mapping of the EntityId_t
 * 
 * NOTE: this implementation ignores Deprecated EntityIds in version 2.1 of the Protocol
 * as specified in 9.3.1.4 
 */
public abstract class EntityId_t  {
	public static final EntityId_t SPDP_BUILTIN_PARTICIPANT_WRITER = new SPDPbuiltinParticipantWriter();
	public static final EntityId_t SPDP_BUILTIN_PARTICIPANT_READER = new SPDPbuiltinParticipantReader();
	
	public static final EntityId_t SEDP_BUILTIN_SUBSCRIPTIONS_WRITER = new SEDPbuiltinSubscriptionsWriter();
	public static final EntityId_t SEDP_BUILTIN_SUBSCRIPTIONS_READER = new SEDPbuiltinSubscriptionsReader();
	public static final EntityId_t SEDP_BUILTIN_PUBLICATIONS_WRITER = new SEDPbuiltinPublicationsWriter();
	public static final EntityId_t SEDP_BUILTIN_PUBLICATIONS_READER = new SEDPbuiltinPublicationsReader();
	public static final EntityId_t SEDP_BUILTIN_TOPIC_WRITER = new SEDPbuiltinTopicWriter();
	public static final EntityId_t SEDP_BUILTIN_TOPIC_READER = new SEDPbuiltinTopicReader();
	
	public static final EntityId_t PARTICIPANT = new Participant();
	public static final EntityId_t BUILTIN_PARTICIPANT_MESSAGE_WRITER = new BuiltinParticipantMessageWriter();
	public static final EntityId_t BUILTIN_PARTICIPANT_MESSAGE_READER = new BuiltinParticipantMessageReader();
	public static final EntityId_t UNKNOWN_ENTITY = new UnknownEntity();
	
	public static final int LENGTH = 4;
	
	private byte[] entityKey;
	private byte entityKind;

	
	private EntityId_t(byte[] entityKey, byte entityKind) {
		this.entityKey = entityKey;
		this.entityKind = entityKind;

		assert entityKey != null && entityKey.length == 3;
	}

	public boolean equals(EntityId_t other) {
		return other != null &&
				entityKey[0] == other.entityKey[0] &&
				entityKey[1] == other.entityKey[1] &&
				entityKey[2] == other.entityKey[2] &&
				entityKind == other.entityKind;
	}

	public int hashCode() {
		return Arrays.hashCode(entityKey) + entityKind;
	}
	
	/**
	 * Checks whether this entity is a builtin entity or not
	 * @return
	 */
	public boolean isBuiltinEntity() {
		return (entityKind & 0xc0) == 0xc0; // @see 9.3.1.2 
	}

	/**
	 * Checks whether this entity is an user defined entity or not
	 * @return
	 */
	public boolean isUserDefinedEntity() {
		return (entityKind & 0xc0) == 0x00; // @see 9.3.1.2
	}

	/**
	 * Checks whether this entity is an user defined entity or not
	 * @return
	 */
	public boolean isVendorSpecifiEntity() {
		return (entityKind & 0xc0) == 0x40; // @see 9.3.1.2
	}

	public static EntityId_t readEntityId(byte[] eKey, int kind) {		
		EntityId_t entityId = null;
		
		int key = (eKey[0] << 24) | (eKey[1] << 16) | (eKey[2] << 8) | kind;
		
		switch (key) {
			case  (0)            : entityId = new UnknownEntity(); break;
			case  (1 << 8) | 0xc1: entityId = new Participant(); break;
			case  (2 << 8) | 0xc2: entityId = new SEDPbuiltinTopicWriter(); break;
			case  (2 << 8) | 0xc7: entityId = new SEDPbuiltinTopicReader(); break;
			case  (3 << 8) | 0xc2: entityId = new SEDPbuiltinPublicationsWriter(); break;
			case  (3 << 8) | 0xc7: entityId = new SEDPbuiltinPublicationsReader(); break;
			case  (4 << 8) | 0xc2: entityId = new SEDPbuiltinSubscriptionsWriter(); break;
			case  (4 << 8) | 0xc7: entityId = new SEDPbuiltinSubscriptionsReader(); break;
			case (1 << 16) | 0xc2: entityId = new SPDPbuiltinParticipantWriter(); break;
			case (1 << 16) | 0xc7: entityId = new SPDPbuiltinParticipantReader(); break;
			case (2 << 16) | 0xc2: entityId = new BuiltinParticipantMessageWriter(); break; // liveliness protocol?
			case (2 << 16) | 0xc7: entityId = new BuiltinParticipantMessageReader(); break; // liveliness protocol?
			default:
				if ((kind & 0x40) == 0x40) { // two most signicant bits equals '01' -> vendor specific entities
					entityId = new VendorSpecificEntityId(eKey, (byte) kind);
				}
				else { // User specific
					entityId = new UserDefinedEntityId(eKey, (byte) kind);
				}	
		}
		
		return entityId;
	}
	
	public static EntityId_t readEntityId(RTPSByteBuffer is) {
		byte[] eKey = new byte[3];
		is.read(eKey);
		int kind = is.read_octet() & 0xff;
	
		return readEntityId(eKey, kind);
	}



	public static class UserDefinedEntityId extends EntityId_t {
		public UserDefinedEntityId(byte[] entityKey, int entityKind) {
			super(entityKey, (byte) entityKind);
		}

		@Override
		public int getEndpointSetId() {
			return 0;
		}
	}

	public static class VendorSpecificEntityId extends EntityId_t {
		public VendorSpecificEntityId(byte[] entityKey, byte entityKind) {
			super(entityKey, entityKind);
		}

		@Override
		public int getEndpointSetId() {
			return 0;
		}
	}


	public static class UnknownEntity extends EntityId_t {
		public UnknownEntity() {
			super(new byte[] {0,0,0}, (byte) 0x00);
		}

		@Override
		public int getEndpointSetId() {
			return 0;
		}
	}

	public static class Participant extends EntityId_t {
		private Participant() {
			super(new byte[] {0,0,1}, (byte) 0xc1);
		}

		@Override
		public int getEndpointSetId() {
			return 0; // TODO: check this
		}
	}

	public static class SEDPbuiltinTopicWriter extends EntityId_t {
		private SEDPbuiltinTopicWriter() {
			super(new byte[] {0,0,2},(byte) 0xc2);
		}

		@Override
		public int getEndpointSetId() {
			return 0; // TODO: check this
		}
	}

	public static class SEDPbuiltinTopicReader extends EntityId_t {
		private SEDPbuiltinTopicReader() {
			super(new byte[] {0,0,2},(byte) 0xc7);
		}

		@Override
		public int getEndpointSetId() {
			return 0; // TODO: check this
		}
	}

	public static class SEDPbuiltinPublicationsWriter extends EntityId_t {
		private SEDPbuiltinPublicationsWriter() {
			super(new byte[] {0,0,3},(byte) 0xc2);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER;
		}
	}

	public static class SEDPbuiltinPublicationsReader extends EntityId_t {
		private SEDPbuiltinPublicationsReader() {
			super(new byte[] {0,0,3},(byte) 0xc7);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR;
		}
	}

	public static class SEDPbuiltinSubscriptionsWriter extends EntityId_t {
		private SEDPbuiltinSubscriptionsWriter() {
			super(new byte[] {0,0,4},(byte) 0xc2);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER;
		}
	}

	public static class SEDPbuiltinSubscriptionsReader extends EntityId_t {
		private SEDPbuiltinSubscriptionsReader() {
			super(new byte[] {0,0,4},(byte) 0xc7);
		}
	
		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR;
		}
	}

	public static class SPDPbuiltinParticipantWriter extends EntityId_t {
		private SPDPbuiltinParticipantWriter() {
			super(new byte[] {0,1,0},(byte) 0xc2);
		}
		
		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER; // TODO: Check this
		}
	}

	public static class SPDPbuiltinParticipantReader extends EntityId_t {
		// NOTE: In spec, this is named SPDPbuiltinSdpParticipantReader
		private SPDPbuiltinParticipantReader() {
			super(new byte[] {0,1,0},(byte) 0xc7);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR; // TODO: Check this
		}
	}

	public static class BuiltinParticipantMessageWriter extends EntityId_t {
		private BuiltinParticipantMessageWriter() {
			super(new byte[] {0,2,0},(byte) 0xc2);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER; // TODO: Check this
		}
	}

	public static class BuiltinParticipantMessageReader extends EntityId_t {
		private BuiltinParticipantMessageReader() {
			super(new byte[] {0,2,0},(byte) 0xc7);
		}

		@Override
		public int getEndpointSetId() {
			return BuiltinEndpointSet_t.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER; // TODO: Check this
		}
}

	public String toString() {
		if (isBuiltinEntity() || this instanceof UnknownEntity) {
			return getClass().getSimpleName();
		}
		else {
			StringBuffer sb = new StringBuffer("[");
			for (int i = 0; i < 2; i++) {
				sb.append(String.format("0x%02x", entityKey[i]) + ",");
			}
			sb.append(String.format("0x%02x", entityKey[2]) + "], " + String.format("0x%02x", entityKind));

			return sb.toString();
		}
	}


	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(entityKey);
		buffer.write_octet(entityKind);
	}

	/**
	 * Gets this EntityId as a byte array. First 3 bytes are key, and last byte is entity kind.
	 *  
	 * @return a byte array of length 4
	 */
	public byte[] getBytes() {
		byte[] bytes = new byte[4];
		System.arraycopy(this.entityKey, 0, bytes, 0, 3);
		bytes[3] = entityKind;
		
		return bytes;
	}


	public abstract int getEndpointSetId();
}
