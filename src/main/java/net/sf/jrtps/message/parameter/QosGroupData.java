package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosGroupData extends Parameter implements SubscriberPolicy<QosGroupData>, PublisherPolicy<QosGroupData> {
    QosGroupData() {
        super(ParameterEnum.PID_GROUP_DATA);
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

    @Override
    public boolean isCompatible(QosGroupData other) {
        return true; // Always true
    }

    public static QosGroupData defaultGroupData() {
        // TODO: check default GroupData
        return new QosGroupData();
    }
}