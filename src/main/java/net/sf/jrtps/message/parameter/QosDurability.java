package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This QoS policy controls whether the Service will actually make data
 * available to late-joining readers.
 * <p>
 * 
 * See DDS specification v1.2, ch. 7.1.3.4
 * 
 * @author mcr70
 * 
 */
public class QosDurability extends Parameter implements DataReaderPolicy<QosDurability>,
        DataWriterPolicy<QosDurability>, TopicPolicy<QosDurability>, InlineParameter {
    private int kind;

    public enum Kind {
        VOLATILE, TRANSIENT_LOCAL, TRANSIENT, PERSISTENT;
    }

    QosDurability() {
        super(ParameterEnum.PID_DURABILITY);
    }

    /**
     * Constructor for QosDurability.
     * 
     * @param kind
     */
    public QosDurability(Kind kind) {
        super(ParameterEnum.PID_DURABILITY);
        switch (kind) {
        case VOLATILE:
            this.kind = 0;
            break;
        case TRANSIENT_LOCAL:
            this.kind = 1;
            break;
        case TRANSIENT:
            this.kind = 2;
            break;
        case PERSISTENT:
            this.kind = 3;
            break;
        }
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(kind);
    }

    public Kind getKind() {
        switch (kind) {
        case 0:
            return Kind.VOLATILE;
        case 1:
            return Kind.TRANSIENT_LOCAL;
        case 2:
            return Kind.TRANSIENT;
        case 3:
            return Kind.PERSISTENT;
        }

        throw new IllegalArgumentException("Unknown kind " + kind + " for QosDurability");
    }

    @Override
    public boolean isCompatible(QosDurability other) {
        QosDurability qOther = (QosDurability) other;
        return kind >= qOther.kind;
    }

    /**
     * Get the default QosDurability: VOLATILE
     * 
     * @return default QosDurability
     */
    public static QosDurability defaultDurability() {
        return new QosDurability(Kind.TRANSIENT);
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ")";
    }
}