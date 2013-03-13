package alt.rtps.types;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * 
 * @author mcr70
 * @see 9.3.1.5 Mapping of the GUID_t
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
	
		
	public boolean equals(GUID_t other) {
		return other != null && prefix.equals(other.prefix) && entityId.equals(other.entityId);
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
//		byte[] prefix_bytes = prefix.getBytes();
//		byte[] entity_bytes = entityId.getBytes();
//		
//		System.arraycopy(prefix_bytes, 0, guid_bytes, 0, prefix_bytes.length);
//		System.arraycopy(entity_bytes, 0, guid_bytes, 12, 4);
		RTPSByteBuffer bb = new RTPSByteBuffer(guid_bytes);
		writeTo(bb);
		
		return guid_bytes;
	}


	public String toString() {
		return prefix.toString() + ", " + entityId.toString();
	}
}
