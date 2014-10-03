package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class TopicName extends Parameter implements InlineQoS {
    private String name;

    public TopicName(String name) {
        super(ParameterId.PID_TOPIC_NAME);
        this.name = name;

    }

    TopicName() {
        super(ParameterId.PID_TOPIC_NAME);
    }

    public String getName() {
        return name; // TODO, @see table 9.14: string<256> vs. rtps_rcps.idl:
                     // string
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        name = bb.read_string();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_string(name);
    }

    public String toString() {
        return super.toString() + "(" + getName() + ")";
    }
}