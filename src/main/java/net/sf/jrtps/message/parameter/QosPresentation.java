package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosPresentation extends Parameter implements PublisherPolicy<QosPresentation>,
        SubscriberPolicy<QosPresentation>, InlineQoS {
    public enum Kind {
        INSTANCE, TOPIC, GROUP
    };

    private int access_scope;
    private boolean coherent_access;
    private boolean ordered_access;

    QosPresentation() {
        super(ParameterId.PID_PRESENTATION);
    }

    public QosPresentation(Kind kind, boolean coherent_access, boolean ordered_access) {
        super(ParameterId.PID_PRESENTATION);
        switch (kind) {
        case INSTANCE:
            this.access_scope = 0;
            break;
        case TOPIC:
            this.access_scope = 1;
            break;
        case GROUP:
            this.access_scope = 2;
            break;
        }

        this.coherent_access = coherent_access;
        this.ordered_access = ordered_access;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.access_scope = bb.read_long();
        this.coherent_access = bb.read_boolean();
        this.ordered_access = bb.read_boolean();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(access_scope);
        bb.write_boolean(coherent_access);
        bb.write_boolean(ordered_access);
    }

    public Kind getKind() {
        switch (access_scope) {
        case 0:
            return Kind.INSTANCE;
        case 1:
            return Kind.TOPIC;
        case 2:
            return Kind.GROUP;
        }

        throw new IllegalArgumentException("Illegal kind " + access_scope + " for QosPresentation");
    }

    public boolean isCoherentAccess() {
        return coherent_access;
    }
    
    public boolean isOrderedAccess() {
        return ordered_access;
    }
    
    @Override
    public boolean isCompatible(QosPresentation other) {
        if (access_scope >= other.access_scope) {
            if ((other.coherent_access == false) || (coherent_access == true && other.coherent_access == true)) {
                if ((other.ordered_access == false) || (ordered_access == true && other.ordered_access == true)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the default QosPresentation: INSTANCE, false, false
     * 
     * @return default QosPresentation
     */
    public static QosPresentation defaultPresentation() {
        return new QosPresentation(Kind.INSTANCE, false, false);
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ", coherent=" + coherent_access + ", ordered=" + ordered_access
                + ")";
    }
}