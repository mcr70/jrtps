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
	
	public String toString() {
		return prefix.toString() + ", " + entityId.toString();
	}

	public void writeTo(RTPSByteBuffer buffer) {
		prefix.writeTo(buffer);
		entityId.writeTo(buffer);
	}
}
