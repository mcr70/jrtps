package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.3.1.5 Mapping of the GUID_t
 * @author mcr70
 * 
 */
public class GUID_t {
	public GuidPrefix_t prefix;
	public EntityId_t entityId;
	
	public GUID_t(GuidPrefix_t prefix, EntityId_t entityId) {
		if (prefix == null || entityId == null) {
			throw new IllegalArgumentException("prefix: " + prefix + ", entityId: " + entityId + ": null is not allowed");
		}
		
		this.prefix = prefix;
		this.entityId = entityId;
	}
	
	public GUID_t(RTPSByteBuffer bb) {
		this.prefix = new GuidPrefix_t(bb);
		this.entityId = EntityId_t.readEntityId(bb);
	}
	
	public GUID_t(byte[] bytes) {
		if (bytes == null || bytes.length != 16) {
			throw new IllegalArgumentException("Length of GUID_t must be 16");
		}
		
		byte[] prefixBytes = new byte[12];
		System.arraycopy(bytes, 0, prefixBytes, 0, 12);
		this.prefix = new GuidPrefix_t(prefixBytes);
		
		byte[] entityBytes = new byte[3];
		System.arraycopy(bytes, 12, entityBytes, 0, 3);
		byte entityKind = bytes[15];
		
		this.entityId = EntityId_t.readEntityId(entityBytes, entityKind);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GUID_t) {
			GUID_t o = (GUID_t) other;
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
