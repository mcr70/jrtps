package net.sf.jrtps.types;

import java.util.Arrays;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.rtps.RTPSParticipant;
import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.3.1.2 Mapping of the EntityId_t
 * 
 * NOTE: this implementation ignores Deprecated EntityIds in version 2.1 of the
 * Protocol as specified in 9.3.1.4
 * 
 * @author mcr70
 */
public abstract class EntityId {
    /**
     * Builtin writer for SPDP ParticipantData
     */
    public static final EntityId SPDP_BUILTIN_PARTICIPANT_WRITER = new SPDPbuiltinParticipantWriter();
    /**
     * Builtin reader for SPDP ParticipantData
     */
    public static final EntityId SPDP_BUILTIN_PARTICIPANT_READER = new SPDPbuiltinParticipantReader();
    /**
     * Builtin writer for subscriptions
     */
    public static final EntityId SEDP_BUILTIN_SUBSCRIPTIONS_WRITER = new SEDPbuiltinSubscriptionsWriter();
    /**
     * Builtin writer for secure subscriptions
     */
    public static final EntityId SEDP_BUILTIN_SUBSCRIPTIONS_SECURE_WRITER = new SEDPbuiltinSubscriptionsSecureWriter();
    /**
     * Builtin reader for subscriptions
     */
    public static final EntityId SEDP_BUILTIN_SUBSCRIPTIONS_READER = new SEDPbuiltinSubscriptionsReader();
    /**
     * Builtin reader for secure subscriptions
     */
    public static final EntityId SEDP_BUILTIN_SUBSCRIPTIONS_SECURE_READER = new SEDPbuiltinSubscriptionsSecureReader();
    /**
     * Builtin writer for publications
     */
    public static final EntityId SEDP_BUILTIN_PUBLICATIONS_WRITER = new SEDPbuiltinPublicationsWriter();
    /**
     * Builtin writer for secure publications
     */
    public static final EntityId SEDP_BUILTIN_PUBLICATIONS_SECURE_WRITER = new SEDPbuiltinPublicationsSecureWriter();
    /**
     * Builtin reader for publications
     */
    public static final EntityId SEDP_BUILTIN_PUBLICATIONS_READER = new SEDPbuiltinPublicationsReader();
    /**
     * Builtin reader for secure publications
     */
    public static final EntityId SEDP_BUILTIN_PUBLICATIONS_SECURE_READER= new SEDPbuiltinPublicationsSecureReader();

    /**
     * Builtin writer for topic
     */
    public static final EntityId SEDP_BUILTIN_TOPIC_WRITER = new SEDPbuiltinTopicWriter();
    /**
     * Builtin reader for topic
     */
    public static final EntityId SEDP_BUILTIN_TOPIC_READER = new SEDPbuiltinTopicReader();
    /**
     * Entity id representing Participant.
     * 
     * @see RTPSParticipant
     */
    public static final EntityId PARTICIPANT = new Participant();
    
    /**
     * Builtin writer for ParticipantMessage
     */
    public static final EntityId BUILTIN_PARTICIPANT_MESSAGE_WRITER = new BuiltinParticipantMessageWriter();
    /**
     * Builtin writer for secure ParticipantMessage
     */
    public static final EntityId BUILTIN_PARTICIPANT_MESSAGE_SECURE_WRITER = new BuiltinParticipantMessageSecureWriter();
    /**
     * Builtin reader for ParticipantMessage
     */
    public static final EntityId BUILTIN_PARTICIPANT_MESSAGE_READER = new BuiltinParticipantMessageReader();
    /**
     * Builtin reader for secure ParticipantMessage
     */
    public static final EntityId BUILTIN_PARTICIPANT_MESSAGE_SECURE_READER = new BuiltinParticipantMessageSecureReader();
    
    public static final EntityId BUILTIN_PARTICIPANT_STATELESS_WRITER = new BuiltinParticipantStatelessWriter();
    public static final EntityId BUILTIN_PARTICIPANT_STATELESS_READER = new BuiltinParticipantStatelessReader();

    public static final EntityId BUILTIN_PARTICIPANT_VOLATILE_WRITER = new BuiltinParticipantVolatileMessageSecureWriter();
    public static final EntityId BUILTIN_PARTICIPANT_VOLATILE_READER = new BuiltinParticipantVolatileMessageSecureReader();

    /**
     * Represents an unknown entity
     */
    public static final EntityId UNKNOWN_ENTITY = new UnknownEntity();

    private byte[] entityKey;
    
    // 0x02:writer_key, 0x03:writer_no_key,
    // 0x04:reader_no_key, 0x07:reader_key
    private byte entityKind;  

    private EntityId(byte[] entityKey, byte entityKind) {
        this.entityKey = entityKey;
        this.entityKind = entityKind;

        assert entityKey != null && entityKey.length == 3;
    }

    public boolean equals(EntityId other) {
        return other != null && entityKey[0] == other.entityKey[0] && entityKey[1] == other.entityKey[1]
                && entityKey[2] == other.entityKey[2] && entityKind == other.entityKind;
    }

    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }

    /**
     * Checks whether this entity is a builtin entity or not
     * 
     * @return true, if this EntityId_t represents a builtin entity
     */
    public boolean isBuiltinEntity() {
        return (entityKind & 0xc0) == 0xc0; // @see 9.3.1.2
    }

    /**
     * Checks whether this entity is an user defined entity or not
     * 
     * @return true, if this EntityId_t represents a user defined entity
     */
    public boolean isUserDefinedEntity() {
        return (entityKind & 0xc0) == 0x00; // @see 9.3.1.2
    }

    /**
     * Checks whether this entity is an user defined entity or not
     * 
     * @return true, if this EntityId_t represents a vendor specific entity
     */
    public boolean isVendorSpecifiEntity() {
        return (entityKind & 0xc0) == 0x40; // @see 9.3.1.2
    }

    public void writeTo(RTPSByteBuffer bb) {
        bb.write(entityKey);
        bb.write_octet(entityKind);
    }

    /**
     * Gets this EntityId as a byte array. First 3 bytes are key, and last byte
     * is entity kind.
     * 
     * @return a byte array of length 4
     */
    public byte[] getBytes() {
        byte[] bytes = new byte[4];
        System.arraycopy(this.entityKey, 0, bytes, 0, 3);
        bytes[3] = entityKind;

        return bytes;
    }

    /**
     * Gets endpoint set id. Endpoint set is used with SPDP ParticipantData to
     * tell remote participant of the builtin endpoints available. Endpoint set
     * id is a bit representing a known entityId
     * 
     * @return endpoint set id.
     */
    public abstract int getEndpointSetId();

    /**
     * Gets the entityKey of this EntityId
     * @return an array of length 3
     */
    public byte[] getEntityKey() {
        return entityKey;
    }
    
    /**
     * Gets the entityKind of this EntityId
     * @return entityKind
     */
    public byte getEntityKind() {
        return entityKind;
    }

    /**
     * Reads EntityId from given byte[] and kind.
     * 
     * @param eKey an array of bytes representing entity key
     * @param kind Kind of entity
     * @return EntityId
     */
    public static EntityId createEntityId(byte[] eKey, int kind) {
        EntityId entityId = null;

        int key = (eKey[0] << 24) | (eKey[1] << 16) | (eKey[2] << 8) | (kind & 0xff);

        switch (key) {
        case (0):
            entityId = new UnknownEntity();
            break;
        case (1 << 8) | 0xc1:
            entityId = new Participant();
            break;
        case (2 << 8) | 0xc2:
            entityId = new SEDPbuiltinTopicWriter();
            break;
        case (2 << 8) | 0xc7:
            entityId = new SEDPbuiltinTopicReader();
            break;
        case (3 << 8) | 0xc2:
            entityId = new SEDPbuiltinPublicationsWriter();
            break;
        case (0xff << 24) | (3 << 8) | 0xc2:
            entityId = new SEDPbuiltinPublicationsSecureWriter(); // from DDS security
            break;
        case (3 << 8) | 0xc7:
            entityId = new SEDPbuiltinPublicationsReader();
            break;
        case (0xff << 24) | (3 << 8) | 0xc7:
            entityId = new SEDPbuiltinPublicationsSecureReader(); // from DDS security
            break;
        case (4 << 8) | 0xc2:
            entityId = new SEDPbuiltinSubscriptionsWriter();
            break;
        case (0xff << 24) | (4 << 8) | 0xc2:
            entityId = new SEDPbuiltinSubscriptionsSecureWriter(); // from DDS security
            break;
        case (4 << 8) | 0xc7:
            entityId = new SEDPbuiltinSubscriptionsReader();
            break;
        case (0xff << 24) | (4 << 8) | 0xc7:
            entityId = new SEDPbuiltinSubscriptionsSecureReader(); // from DDS security
            break;
        case (1 << 16) | 0xc2:
            entityId = new SPDPbuiltinParticipantWriter();
            break;
        case (1 << 16) | 0xc7:
            entityId = new SPDPbuiltinParticipantReader();
            break;
        case (2 << 16) | 0xc2:
            entityId = new BuiltinParticipantMessageWriter();
            break; 
        case (0xff << 24) | (2 << 16) | 0xc2:
            entityId = new BuiltinParticipantMessageSecureWriter(); // from DDS security
            break; 
        case (2 << 16) | 0xc7:
            entityId = new BuiltinParticipantMessageReader();
            break; 
        case (0xff << 24) | (2 << 16) | 0xc7:
            entityId = new BuiltinParticipantMessageSecureReader();
            break; 
        case (2 << 16) | (1 << 8) | 0xc2:
            entityId = new BuiltinParticipantStatelessWriter(); // from DDS security
            break;  
        case (2 << 16) | (1 << 8) | 0xc7:
            entityId = new BuiltinParticipantStatelessReader(); // from DDS security
            break;  
        case (0xff << 24) | (2 << 16) | (2 << 8) | 0xc2:
            entityId = new BuiltinParticipantVolatileMessageSecureWriter(); // from DDS security
            break;  
        case (0xff << 24) | (2 << 16) | (2 << 8) | 0xc7:
            entityId = new BuiltinParticipantVolatileMessageSecureWriter(); // from DDS security
            break;  
            
        default:
            if ((kind & 0x40) == 0x40) { // two most signicant bits equals '01'
                                         // -> vendor specific entities
                entityId = new VendorSpecificEntityId(eKey, (byte) kind);
            } else { // User specific
                entityId = new UserDefinedEntityId(eKey, (byte) kind);
            }
        }

        return entityId;
    }

    /**
     * Reads an EntityId from RTPSByteBuffer.
     * 
     * @param bb RTPSByteBuffer
     * @return EntityId
     */
    public static EntityId readEntityId(RTPSByteBuffer bb) {
        byte[] eKey = new byte[3];
        bb.read(eKey);
        int kind = bb.read_octet() & 0xff;

        return createEntityId(eKey, kind);
    }

    /**
     * EntityId representing an user defined entity.
     * 
     * @author mcr70
     * 
     */
    public static class UserDefinedEntityId extends EntityId {
        public UserDefinedEntityId(byte[] entityKey, int entityKind) {
            super(entityKey, (byte) entityKind);
        }

        @Override
        public int getEndpointSetId() {
            return 0;
        }
    }

    /**
     * EntityId representing a vendor specific entity.
     * 
     * @author mcr70
     * 
     */
    public static class VendorSpecificEntityId extends EntityId {
        public VendorSpecificEntityId(byte[] entityKey, byte entityKind) {
            super(entityKey, entityKind);
        }

        @Override
        public int getEndpointSetId() {
            return 0;
        }
    }

    /**
     * EntityId representing an unknown entity.
     * 
     * @author mcr70
     * 
     */
    public static class UnknownEntity extends EntityId {
        public UnknownEntity() {
            super(new byte[] { 0, 0, 0 }, (byte) 0x00);
        }

        @Override
        public int getEndpointSetId() {
            return 0;
        }
    }

    /**
     * EntityId representing RTPS Participant.
     * 
     * @author mcr70
     * 
     */
    public static class Participant extends EntityId {
        private Participant() {
            super(new byte[] { 0, 0, 1 }, (byte) 0xc1);
        }

        @Override
        public int getEndpointSetId() {
            return 0; 
        }
    }

    /**
     * EntityId representing SEDP builtin topic writer.
     * @author mcr70
     */
    public static class SEDPbuiltinTopicWriter extends EntityId {
        private SEDPbuiltinTopicWriter() {
            super(new byte[] { 0, 0, 2 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return 0; // TODO: check this
        }
    }

    /**
     * EntityId representing SEDP builtin topic reader.
     * @author mcr70
     */
    public static class SEDPbuiltinTopicReader extends EntityId {
        private SEDPbuiltinTopicReader() {
            super(new byte[] { 0, 0, 2 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return 0; // TODO: check this
        }
    }

    /**
     * Used with DDS security. The BuiltinParticipantStatelessWriter is an RTPS 
     * Best-Effort StatelessWriter
     * @author mcr70
     */
    public static class BuiltinParticipantStatelessWriter extends EntityId {
        private BuiltinParticipantStatelessWriter() {
            super(new byte[] { 0, 2, 1 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return 0;
        }
    }

    /**
     * Used with DDS security. The BuiltinParticipantStatelessReader is an RTPS 
     * Best-Effort StatelessReader
     * @author mcr70
     */
    public static class BuiltinParticipantStatelessReader extends EntityId {
        private BuiltinParticipantStatelessReader() {
            super(new byte[] { 0, 2, 1 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return 0;
        }
    }

    /**
     * Used with DDS security. 
     * @author mcr70
     */
    public static class BuiltinParticipantVolatileMessageSecureWriter extends EntityId {
        private BuiltinParticipantVolatileMessageSecureWriter() {
            super(new byte[] { (byte) 0xff, 2, 2 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return 0; 
        }
    }

    /**
     * Used with DDS security. 
     * @author mcr70
     */
    public static class BuiltinParticipantVolatileMessageSecureReader extends EntityId {
        private BuiltinParticipantVolatileMessageSecureReader() {
            super(new byte[] { (byte) 0xff, 2, 2 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return 0; 
        }
    }
    

    /**
     * EntityId representing SEDP builtin publications writer.
     * 
     * @see net.sf.jrtps.builtin.PublicationData
     * @author mcr70
     */
    public static class SEDPbuiltinPublicationsWriter extends EntityId {
        private SEDPbuiltinPublicationsWriter() {
            super(new byte[] { 0, 0, 3 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER;
        }
    }

    /**
     * EntityId representing SEDP builtin publications secure writer.
     * 
     * @see net.sf.jrtps.builtin.PublicationData
     * @author mcr70
     */
    public static class SEDPbuiltinPublicationsSecureWriter extends EntityId {
        private SEDPbuiltinPublicationsSecureWriter() {
            super(new byte[] { (byte) 0xff, 0, 3 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER;
        }
    }

    /**
     * EntityId representing SEDP builtin publications reader.
     * 
     * @see net.sf.jrtps.builtin.PublicationData
     * @author mcr70
     */
    public static class SEDPbuiltinPublicationsReader extends EntityId {
        private SEDPbuiltinPublicationsReader() {
            super(new byte[] { 0, 0, 3 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR;
        }
    }

    /**
     * EntityId representing SEDP builtin publications secure reader.
     * 
     * @see net.sf.jrtps.builtin.PublicationData
     * @author mcr70
     */
    public static class SEDPbuiltinPublicationsSecureReader extends EntityId {
        private SEDPbuiltinPublicationsSecureReader() {
            super(new byte[] { (byte) 0xff, 0, 3 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR;
        }
    }

    /**
     * EntityId representing SEDP builtin subscriptions writer.
     * 
     * @see net.sf.jrtps.builtin.SubscriptionData
     * @author mcr70
     */
    public static class SEDPbuiltinSubscriptionsWriter extends EntityId {
        private SEDPbuiltinSubscriptionsWriter() {
            super(new byte[] { 0, 0, 4 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER;
        }
    }

    /**
     * EntityId representing SEDP builtin subscriptions secure writer.
     * 
     * @see net.sf.jrtps.builtin.SubscriptionData
     * @author mcr70
     */
    public static class SEDPbuiltinSubscriptionsSecureWriter extends EntityId {
        private SEDPbuiltinSubscriptionsSecureWriter() {
            super(new byte[] { (byte) 0xff, 0, 4 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER;
        }
    }

    /**
     * EntityId representing SEDP builtin subscriptions reader.
     * 
     * @see net.sf.jrtps.builtin.SubscriptionData
     * @author mcr70
     */
    public static class SEDPbuiltinSubscriptionsReader extends EntityId {
        private SEDPbuiltinSubscriptionsReader() {
            super(new byte[] { 0, 0, 4 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR;
        }
    }

    /**
     * EntityId representing SEDP builtin subscriptions secure reader.
     * 
     * @see net.sf.jrtps.builtin.SubscriptionData
     * @author mcr70
     */
    public static class SEDPbuiltinSubscriptionsSecureReader extends EntityId {
        private SEDPbuiltinSubscriptionsSecureReader() {
            super(new byte[] { (byte) 0xff, 0, 4 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR;
        }
    }

    /**
     * EntityId representing SPDP builtin participant writer.
     * 
     * @see ParticipantData
     * @author mcr70
     */
    public static class SPDPbuiltinParticipantWriter extends EntityId {
        private SPDPbuiltinParticipantWriter() {
            super(new byte[] { 0, 1, 0 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER;
        }
    }

    /**
     * EntityId representing SPDP builtin participant reader.
     * 
     * @see ParticipantData
     * @author mcr70
     */
    public static class SPDPbuiltinParticipantReader extends EntityId {
        // NOTE: In spec, this is named SPDPbuiltinSdpParticipantReader
        private SPDPbuiltinParticipantReader() {
            super(new byte[] { 0, 1, 0 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR;
        }
    }

    /**
     * EntityId representing builtin ParticipantMessage writer.
     * ParticipantMessages are used with writer liveliness protocol.
     * 
     * @see ParticipantMessage
     * @author mcr70
     */
    public static class BuiltinParticipantMessageWriter extends EntityId {
        private BuiltinParticipantMessageWriter() {
            super(new byte[] { 0, 2, 0 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER;
        }
    }

    /**
     * EntityId representing builtin ParticipantMessage secure writer.
     * ParticipantMessages are used with writer liveliness protocol.
     * 
     * @see ParticipantMessage
     * @author mcr70
     */
    public static class BuiltinParticipantMessageSecureWriter extends EntityId {
        private BuiltinParticipantMessageSecureWriter() {
            super(new byte[] { (byte) 0xff, 2, 0 }, (byte) 0xc2);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER;
        }
    }

    /**
     * EntityId representing builtin ParticipantMessage reader.
     * ParticipantMessages are used with writer liveliness protocol.
     * 
     * @see ParticipantMessage
     * @author mcr70
     */
    public static class BuiltinParticipantMessageReader extends EntityId {
        private BuiltinParticipantMessageReader() {
            super(new byte[] { 0, 2, 0 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER;
        }
    }

    /**
     * EntityId representing builtin secure ParticipantMessage reader.
     * ParticipantMessages are used with writer liveliness protocol.
     * 
     * @see ParticipantMessage
     * @author mcr70
     */
    public static class BuiltinParticipantMessageSecureReader extends EntityId {
        private BuiltinParticipantMessageSecureReader() {
            super(new byte[] { (byte) 0xff, 2, 0 }, (byte) 0xc7);
        }

        @Override
        public int getEndpointSetId() {
            return BuiltinEndpointSet.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER;
        }
    }

    public String toString() {
        if (isBuiltinEntity() || this instanceof UnknownEntity) {
            return getClass().getSimpleName();
        } else {
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < 2; i++) {
                sb.append(String.format("0x%02x", entityKey[i]) + ",");
            }
            sb.append(String.format("0x%02x", entityKey[2]) + "], " + String.format("0x%02x", entityKind));

            return sb.toString();
        }
    }
}
