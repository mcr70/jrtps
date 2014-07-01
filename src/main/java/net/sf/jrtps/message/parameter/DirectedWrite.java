package net.sf.jrtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

public class DirectedWrite extends Parameter implements InlineParameter {
    private List<Guid> guids;

    DirectedWrite() {
        super(ParameterEnum.PID_DIRECTED_WRITE);
    }

    public DirectedWrite(List<Guid> guids) {
        super(ParameterEnum.PID_DIRECTED_WRITE);
        this.guids = guids;        
    }
    
    /**
     * Gets the guids of this DirectedWriter
     * @return a List of Guids
     */
    public List<Guid> getGuids() {
        return guids;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        guids = new LinkedList<>();
        int size = bb.read_long();
        for (int i = 0; i < size; i++) {
            guids.add(new Guid(bb));
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(guids.size());
        for (Guid guid : guids) {
            guid.writeTo(bb);
        }
    }
}