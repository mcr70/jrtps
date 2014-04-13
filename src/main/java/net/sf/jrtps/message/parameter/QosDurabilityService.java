package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosDurabilityService extends Parameter implements DataWriterPolicy<QosDurabilityService>,
        TopicPolicy<QosDurabilityService> {
    QosDurabilityService() {
        super(ParameterEnum.PID_DURABILITY_SERVICE);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }

    @Override
    public boolean isCompatible(QosDurabilityService other) {
        return true; // Always true
    }

    public static QosDurabilityService defaultDurabilityService() {
        return new QosDurabilityService();
    }
}