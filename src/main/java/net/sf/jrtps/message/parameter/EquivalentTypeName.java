package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class EquivalentTypeName extends Parameter {
    String[] equivalentTypeNames;
    
    EquivalentTypeName() {
        super(ParameterEnum.PID_EQUIVALENT_TYPE_NAME);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        equivalentTypeNames = new String[bb.read_long()];
        for (int i = 0; i < equivalentTypeNames.length; i++) {
            equivalentTypeNames[i] = bb.read_string();
        }
    }

    public String[] getEquivalentTypeNames() {
        return equivalentTypeNames;
    }
    
    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(equivalentTypeNames.length);
        for (String s : equivalentTypeNames) {
            bb.write_string(s);
        }
    }
}
