package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class TypeMaxSizeSerialized extends Parameter {
    int typeMaxSizeSerialized;
	
    TypeMaxSizeSerialized() {
        super(ParameterId.PID_TYPE_MAX_SIZE_SERIALIZED);
    }

    public int getTypeMaxSizeSerialized() {
		return typeMaxSizeSerialized;
	}
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        typeMaxSizeSerialized = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(typeMaxSizeSerialized);
    }
}