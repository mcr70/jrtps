package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class Module implements Marshallable{
    char[] objectName = new char[256];

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        // TODO Auto-generated method stub
        
    }
}