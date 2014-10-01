package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class BaseTypeName extends Parameter {
    String[] baseTypeNames;
    
    BaseTypeName() {
        super(ParameterEnum.PID_BASE_TYPE_NAME);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        baseTypeNames = new String[bb.read_long()];
        for (int i = 0; i < baseTypeNames.length; i++) {
            baseTypeNames[i] = bb.read_string();
        }
    }

    public String[] getBaseTypeNames() {
        return baseTypeNames;
    }
    
    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(baseTypeNames.length);
        for (String s : baseTypeNames) {
            bb.write_string(s);
        }
    }
}
