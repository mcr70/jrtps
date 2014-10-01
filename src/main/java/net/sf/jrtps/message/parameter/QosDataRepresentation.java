package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This QosPolicy is defined in DDS X-Types specification formal-12-11-20-2.pdf, chapter 7.6.2.1 
 * @author mcr70
 */
public class QosDataRepresentation extends Parameter implements DataReaderPolicy<QosDataRepresentation>,
DataWriterPolicy<QosDataRepresentation>, TopicPolicy<QosDataRepresentation>, InlineQoS {
    public static final short XCDR_DATA_REPRESENTATION = 0;
    public static final short XML_DATA_REPRESENTATION = 1;
    
    private short[] values = new short[0]; 
    
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

    public short[] getValues() {
        return values;
    }
    
    /**
     * Get a default DataRepresentation QoS policy.
     * @return QosDataRepresentation with XCDR_DATA_REPRESENTATION
     */
    public static QosDataRepresentation defaultDataRepresentation() {
        return new QosDataRepresentation(); // Defaults to XCDR
    }
    
    @Override
    public boolean isCompatible(QosDataRepresentation requested) {
        short val = values.length > 0 ? values[0] : XCDR_DATA_REPRESENTATION;
        
        for (short s : requested.values) {
            if (s == val) { // see x-types doc 7.6.2.1
                return true;
            }
        }
        
        return false;
    }
}
