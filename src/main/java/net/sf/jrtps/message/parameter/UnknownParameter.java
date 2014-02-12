package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class UnknownParameter extends Parameter {
    private final short paramId;

    protected UnknownParameter(short paramId) {
        super(ParameterEnum.PID_UNKNOWN_PARAMETER);

        this.paramId = paramId;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb); // TODO: default writing. just writes byte[] in super
                        // class
    }

    public String toString() {
        return super.toString() + ", ID " + paramId + ": " + Arrays.toString(getBytes());
    }
}
