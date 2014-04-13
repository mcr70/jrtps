package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class GroupGuid extends Parameter {
    GroupGuid() {
        super(ParameterEnum.PID_GROUP_GUID);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }
}