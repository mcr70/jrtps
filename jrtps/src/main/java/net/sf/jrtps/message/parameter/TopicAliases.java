package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class TopicAliases extends Parameter {

    protected TopicAliases() {
	super(ParameterId.PID_TOPIC_ALIASES);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
	super.readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
	super.writeBytes(bb);
    }
}
