package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Guid uniquely represents an entity.
 * 
 * see 9.3.1.5 Mapping of the GUID_t
 * 
 * @author mcr70
 * 
 */
public class Guid implements Comparable<Guid> {
    public static final Guid GUID_UNKNOWN = 
    		new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.UNKNOWN_ENTITY);
    
	private final GuidPrefix prefix;
    private final EntityId entityId;

    public Guid(GuidPrefix prefix, EntityId entityId) {
        if (prefix == null || entityId == null) {
            throw new IllegalArgumentException("prefix: " + prefix + ", entityId: " + entityId
                    + ": null is not allowed");
        }

        this.prefix = prefix;
        this.entityId = entityId;
    }

    public Guid(RTPSByteBuffer bb) {
        this.prefix = new GuidPrefix(bb);
        this.entityId = EntityId.readEntityId(bb);
    }

    public Guid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Length of Guid must be 16");
        }

        byte[] prefixBytes = new byte[12];
        System.arraycopy(bytes, 0, prefixBytes, 0, 12);
        this.prefix = new GuidPrefix(prefixBytes);

        byte[] entityBytes = new byte[3];
        System.arraycopy(bytes, 12, entityBytes, 0, 3);
        byte entityKind = bytes[15];

        this.entityId = EntityId.createEntityId(entityBytes, entityKind);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Guid) {
            Guid o = (Guid) other;
            return getPrefix().equals(o.getPrefix()) && getEntityId().equals(o.getEntityId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPrefix().hashCode() + getEntityId().hashCode();
    }

    public void writeTo(RTPSByteBuffer buffer) {
        getPrefix().writeTo(buffer);
        getEntityId().writeTo(buffer);
    }

    /**
     * Gets this GUID as a byte array.
     * 
     * @return a copy of the byte array representing this GUID
     */
    public byte[] getBytes() {
        byte[] guid_bytes = new byte[16];

        RTPSByteBuffer bb = new RTPSByteBuffer(guid_bytes);
        writeTo(bb);

        return guid_bytes;
    }

    /**
     * Gets the GuidPrefix. GuidPrefix represents a Participant. All the
     * entities created by one Participant share the same GuidPrefix.
     * 
     * @return GuidPrefix
     */
    public GuidPrefix getPrefix() {
        return prefix;
    }

    /**
     * Gets the EntityId.
     * 
     * @return EntityId
     */
    public EntityId getEntityId() {
        return entityId;
    }

    public String toString() {
        return getPrefix().toString() + ", " + getEntityId().toString();
    }

    @Override
    public int compareTo(Guid o) {
        byte[] prefixBytes1 = prefix.getBytes();
        byte[] prefixBytes2 = o.prefix.getBytes();
        
        for (int i = 0; i < prefixBytes1.length; i++) {
            if (prefixBytes1[i] == prefixBytes2[i]) {
                continue;
            }
            
            return prefixBytes1[i] < prefixBytes2[i] ? -1 : 1;
        }
        
        byte[] entityBytes1 = entityId.getBytes();
        byte[] entityBytes2 = o.entityId.getBytes();
        
        for (int i = 0; i < entityBytes1.length; i++) {
            if (entityBytes1[i] == entityBytes2[i]) {
                continue;
            }
            
            return (entityBytes1[i] < entityBytes2[i]) ? -1 : 1; 
        }
        
        return 0;
    }
}
