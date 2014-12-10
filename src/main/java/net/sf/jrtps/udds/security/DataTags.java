package net.sf.jrtps.udds.security;

import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class DataTags extends Parameter {

    private DataTag[] data_tags;

    public DataTags(DataTag[] data_tags) {
        super(ParameterId.PID_DATA_TAGS);
        if (data_tags == null) {
            data_tags = new DataTag[0];
        }
        this.data_tags = data_tags;
    }
    
    public DataTags() {
        super(ParameterId.PID_DATA_TAGS);
    }

    public DataTag[] getDataTags() {
        return data_tags;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        int count = bb.read_long();
        for (int i = 0; i < count; i++) {
            data_tags[i] = new DataTag(bb);
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(data_tags.length);
        for (int i = 0; i < data_tags.length; i++) {
            data_tags[i].writeTo(bb);
        }
    }
}
