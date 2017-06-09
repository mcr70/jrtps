package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

/**
 * Represents a related entity. This is used with DDS-RPC to announce 
 * both request/response readers and writers atomically.
 */
public class RelatedEntityGuid extends Parameter {

    private Guid guid;

    protected RelatedEntityGuid() {
	super(ParameterId.PID_RELATED_ENTITY_GUID);
    }

    /**
     * Gets the guid of related entity
     */
    public Guid getGuid() {
	return guid;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
	this.guid = new Guid(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
	this.guid.writeTo(bb);
    }
}
