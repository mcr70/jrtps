package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

public class DirectedWrite extends Parameter implements InlineQoS {
    private Guid[] guids;

    DirectedWrite() {
        super(ParameterId.PID_DIRECTED_WRITE);
    }

    public DirectedWrite(Guid[] guids) {
        super(ParameterId.PID_DIRECTED_WRITE);
        this.guids = guids;        
    }
    
    /**
     * Gets the guids of this DirectedWriter
     * @return an array of Guids
     */
    public Guid[] getGuids() {
        return guids;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        int size = bb.read_long();
        this.guids = new Guid[size];
        for (int i = 0; i < size; i++) {
            guids[i] = new Guid(bb);
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(guids.length);
        for (Guid guid : guids) {
            guid.writeTo(bb);
        }
    }
}