package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosUserData extends Parameter implements DataReaderPolicy<QosUserData>, DataWriterPolicy<QosUserData> {
    QosUserData() {
        super(ParameterEnum.PID_USER_DATA);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb); // TODO: default writing. just writes byte[] in super class
    }

    @Override
    public boolean isCompatible(QosUserData other) {
        return true; // Always true
    }

    public static QosUserData defaultUserData() {
        // TODO: check default UserData
        return new QosUserData();
    }
}