package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Guid uniquely represents an entity.
 * 
 * see 9.3.1.5 Mapping of the GUID_t
 * @author mcr70
 * 
 */
public class Guid {
	public GuidPrefix prefix;
	public EntityId entityId;
	
	public Guid(GuidPrefix prefix, EntityId entityId) {
		if (prefix == null || entityId == null) {
			throw new IllegalArgumentException("prefix: " + prefix + ", entityId: " + entityId + ": null is not allowed");
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
			throw new IllegalArgumentException("Length of GUID_t must be 16");
		}
		
		byte[] prefixBytes = new byte[12];
		System.arraycopy(bytes, 0, prefixBytes, 0, 12);
		this.prefix = new GuidPrefix(prefixBytes);
		
		byte[] entityBytes = new byte[3];
		System.arraycopy(bytes, 12, entityBytes, 0, 3);
		byte entityKind = bytes[15];
		
		this.entityId = EntityId.readEntityId(entityBytes, entityKind);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Guid) {
			Guid o = (Guid) other;
			return o != null && prefix.equals(o.prefix) && entityId.equals(o.entityId);	
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return prefix.hashCode() + entityId.hashCode();
	}

	public void writeTo(RTPSByteBuffer buffer) {
		prefix.writeTo(buffer);
		entityId.writeTo(buffer);
	}

	/**
	 * Gets this GUID as a byte array. 
	 * @return a copy of the byte array representing this GUID
	 */
	public byte[] getBytes() {
		byte[] guid_bytes = new byte[16];

		RTPSByteBuffer bb = new RTPSByteBuffer(guid_bytes);
		writeTo(bb);
		
		return guid_bytes;
	}


	public String toString() {
		return prefix.toString() + ", " + entityId.toString();
	}
}
