package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

/**
 * This policy controls the mechanism and parameters used by the Service to
 * ensure that particular entities on the network are still “alive.”
 * <p>
 * 
 * See DDS specification v1.2, 7.1.3.11 Liveliness
 * 
 * @author mcr70
 * 
 */
public class QosLiveliness extends Parameter implements DataReaderPolicy<QosLiveliness>,
        DataWriterPolicy<QosLiveliness>, TopicPolicy<QosLiveliness>, InlineQoS {
    private int kind;
    private Duration lease_duration;

    public enum Kind {
        /**
         * With this Kind, Participant automatically manages liveliness of
         * Writers having this Kind.
         */
        AUTOMATIC,
        /**
         * With this Kind, it is sufficient that one Writer within Participant
         * asserts its liveliness.
         */
        MANUAL_BY_PARTICIPANT,
        /**
         * Writer that has this kind of Liveliness, has to manage liveliness by
         * itself by writing some samples, or by calling assertLiveliness
         * explicitly.
         */
        MANUAL_BY_TOPIC
    }

    QosLiveliness() {
        super(ParameterEnum.PID_LIVELINESS);
    }

    /**
     * Constructor of QosLiveliness.
     * 
     * @param kind
     *            Kind of liveliness
     * @param lease_duration
     *            Duration of the lease. If a Writer fails to assert liveliness
     *            with lease_duration, it is assumed 'dead'
     * @see Kind
     */
    public QosLiveliness(Kind kind, Duration lease_duration) {
        super(ParameterEnum.PID_LIVELINESS);
        switch (kind) {
        case AUTOMATIC:
            this.kind = 0;
            break;
        case MANUAL_BY_PARTICIPANT:
            this.kind = 1;
            break;
        case MANUAL_BY_TOPIC:
            this.kind = 2;
            break;
        }

        this.lease_duration = lease_duration;
    }

    /**
     * Get the lease_duration
     * 
     * @return lease_duration
     */
    public Duration getLeaseDuration() {
        return lease_duration;
    }

    public Kind getKind() {
        switch (kind) {
        case 0:
            return Kind.AUTOMATIC;
        case 1:
            return Kind.MANUAL_BY_PARTICIPANT;
        case 2:
            return Kind.MANUAL_BY_TOPIC;
        }

        throw new IllegalArgumentException("Illegal kind " + kind + " for QosLiveliness");
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
        this.lease_duration = new Duration(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(kind);
        lease_duration.writeTo(buffer);
    }

    /**
     * Check if this QosLiveliness is compatible with requested. It is
     * considered compatible, if
     * <p>
     * 
     * <blockquote> <i>this.kind >= requested.kind</i> <b>AND</b><br>
     * <i>this.lease_duration <= requested.lease_duration</i> </blockquote>
     * 
     * For the comparison, Kind is ordered like AUTOMATIC <
     * MANUAL_BY_PARTICIPANT < MANUAL_BY_TOPIC
     * 
     * @see Kind
     * @return true, if compatible
     */
    @Override
    public boolean isCompatible(QosLiveliness requested) {
        if ((kind >= requested.kind) && (lease_duration.asMillis() <= requested.lease_duration.asMillis())) {
            return true;
        }
        return false;
    }

    public static QosLiveliness defaultLiveliness() {
        return new QosLiveliness(Kind.AUTOMATIC, Duration.INFINITE); // Automatic, infinite
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ", " + lease_duration + ")";
    }
}