package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * TypeObject
 * @author mcr70
 *
 */
public class TypeObject extends Parameter {
    private TypeLibrary typeLibrary;
    
    protected TypeObject() {
        super(ParameterId.PID_TYPE_OBJECT);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        typeLibrary = new TypeLibrary(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        // TODO Auto-generated method stub

    }
}


