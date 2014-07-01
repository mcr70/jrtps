package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This QosPolicy determines how Readers form the timestamp of the samples.
 * <p>
 * 
 * See DDS specification v1.2, ch. 7.1.3.17
 * 
 * @author mcr70
 * 
 */
public class QosDestinationOrder extends Parameter implements DataReaderPolicy<QosDestinationOrder>,
        DataWriterPolicy<QosDestinationOrder>, TopicPolicy<QosDestinationOrder>, InlineQoS {
    private int kind;

    public enum Kind {
        BY_RECEPTION_TIMESTAMP, BY_SOURCE_TIMESTAMP
    }

    /**
     * Constructor used when reading from RTPSByteBuffer.
     */
    QosDestinationOrder() {
        super(ParameterEnum.PID_DESTINATION_ORDER);
    }

    /**
     * Create a QosDestinationOrder.
     * 
     * @param kind
     */
    public QosDestinationOrder(Kind kind) {
        super(ParameterEnum.PID_DESTINATION_ORDER);
        switch (kind) {
        case BY_RECEPTION_TIMESTAMP:
            this.kind = 0;
            break;
        case BY_SOURCE_TIMESTAMP:
            this.kind = 1;
            break;
        }
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
        if (kind != 0 && kind != 1) {
            kind = -1; // for isCompatible(): never compatible
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(kind);
    }

    @Override
    public boolean isCompatible(QosDestinationOrder other) {
        QosDestinationOrder qOther = (QosDestinationOrder) other;

        return kind >= qOther.kind;
    }

    public Kind getKind() {
        switch (kind) {
        case 0:
            return Kind.BY_RECEPTION_TIMESTAMP;
        case 1:
            return Kind.BY_SOURCE_TIMESTAMP;
        }

        throw new IllegalArgumentException("Illegal kind " + kind + " for QosDestinationOrder");
    }

    /**
     * Get the default QosDestinationOrder BY_RECEPTION_TIMESTAMP.
     * 
     * @return default QosDestinationOrder
     */
    public static QosDestinationOrder defaultDestinationOrder() {
        return new QosDestinationOrder(Kind.BY_RECEPTION_TIMESTAMP);
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ")";
    }
}