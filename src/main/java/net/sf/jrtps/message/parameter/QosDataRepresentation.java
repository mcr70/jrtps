package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosDataRepresentation extends Parameter implements DataReaderPolicy<QosDataRepresentation>,
DataWriterPolicy<QosDataRepresentation>, TopicPolicy<QosDataRepresentation>, InlineQoS {
    public static final short XCDR_DATA_REPRESENTATION = 0;
    public static final short XML_DATA_REPRESENTATION = 1;
    
    private short[] values; // TODO: check array type: short[], int[], ...
    
    QosDataRepresentation() {
        super(ParameterEnum.PID_DATA_REPRESENTATION);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.values = new short[bb.read_long()];
        for (int i = 0; i < values.length; i++) {
            values[i] = bb.readShort();
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(values.length);
        for (int i = 0; i < values.length; i++) {
            bb.writeShort(values[i]);
        }
    }

    @Override
    public boolean isCompatible(QosDataRepresentation requested) {
        for (short s : requested.values) {
            if (s == values[0]) { // see x-types doc
                return true;
            }
        }
        
        return false;
    }
}
