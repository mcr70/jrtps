package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class TypeName extends Parameter {
    private String typeName;

    TypeName() {
        super(ParameterId.PID_TYPE_NAME);
    }

    public TypeName(String typeName) {
        super(ParameterId.PID_TYPE_NAME);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName; // TODO, @see table 9.14: string<256> vs.
                         // rtps_rcps.idl: string
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        typeName = bb.read_string();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_string(getTypeName());
    }

    public String toString() {
        return super.toString() + "(" + getTypeName() + ")";
    }
}