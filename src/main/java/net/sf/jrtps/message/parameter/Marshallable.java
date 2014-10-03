package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

interface Marshallable {
    public void writeTo(RTPSByteBuffer bb);
}