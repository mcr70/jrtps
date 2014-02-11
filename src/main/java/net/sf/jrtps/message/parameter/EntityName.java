package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class EntityName extends Parameter {
    EntityName() {
        super(ParameterEnum.PID_ENTITY_NAME);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length); // TODO: default reading. just reads to byte[] in
                               // super class.
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb); // TODO: default writing. just writes byte[] in super
                        // class
    }
}