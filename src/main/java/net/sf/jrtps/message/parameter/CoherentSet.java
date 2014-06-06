package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.SequenceNumber;

public class CoherentSet extends Parameter implements InlineParameter {
    private SequenceNumber seqNum;
    
    CoherentSet() {
        super(ParameterEnum.PID_COHERENT_SET);
    }

    /**
     * Gets the sequence number of the first sample in coherent set.
     * @return sequence number
     */
    public SequenceNumber getStartSeqNum() {
        return seqNum;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.seqNum = new SequenceNumber(bb); 
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        seqNum.writeTo(bb);
    }
}