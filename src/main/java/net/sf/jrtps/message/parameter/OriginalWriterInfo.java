package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;

/**
 * This class holds information needed to support Persistent level of DDS Durability QoS.
 *  
 * @author mcr70
 */
public class OriginalWriterInfo extends Parameter implements InlineQoS {
    private Guid originalWriterGuid;
    private SequenceNumber originalWriterSn;
    private ParameterList originalWriterQos;
    
    
    public OriginalWriterInfo(Guid originalWriterGuid, SequenceNumber originalWriterSn, ParameterList originalWriterQos) {
        super(ParameterEnum.PID_ORIGINAL_WRITER_INFO);

        this.originalWriterGuid = originalWriterGuid;
        this.originalWriterSn = originalWriterSn;
        this.originalWriterQos = originalWriterQos;
    }
    
    /**
     * Gets the Guid of the writer that originally wrote the Data sample this parameter is associated with.
     * @return Guid
     */
    public Guid getOriginalWriterGuid() {
        return originalWriterGuid;
    }
    
    /**
     * Gets the original sequence number of the Data.  
     * @return SequenceNumber
     */
    public SequenceNumber getOriginalSequenceNumber() {
        return originalWriterSn;
    }
    
    /**
     * Gets the QoS of the writer that original wrote the Data.
     * @return ParameterList
     */
    public ParameterList getOriginalWriterQoS() {
        return originalWriterQos;
    }
    
    OriginalWriterInfo() {
        super(ParameterEnum.PID_ORIGINAL_WRITER_INFO);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        originalWriterGuid = new Guid(bb);
        originalWriterSn = new SequenceNumber(bb);
        originalWriterQos = new ParameterList(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        originalWriterGuid.writeTo(bb);
        originalWriterSn.writeTo(bb);
        originalWriterQos.writeTo(bb);
    }
}