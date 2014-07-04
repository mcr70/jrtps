package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosTypeConsistencyEnforcement extends Parameter implements DataReaderPolicy<QosTypeConsistencyEnforcement> {

    public enum Kind {
        DISALLOW_TYPE_COERCION,
        ALLOW_TYPE_COERCION
    }

    private int kind;

    public QosTypeConsistencyEnforcement() {
        super(ParameterEnum.PID_TYPE_CONSISTENCY_ENFORCEMENT);
    }

    public Kind getKind() {
        switch(kind) {
        case 0: return Kind.DISALLOW_TYPE_COERCION;
        case 1: return Kind.ALLOW_TYPE_COERCION;
        }
        
        throw new IllegalArgumentException("Illegal kind " + kind + " for QosTypeConsistency");
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(kind);
    }

    @Override
    public boolean isCompatible(QosTypeConsistencyEnforcement requested) {
        return true; // applies only to readers
    }
}
