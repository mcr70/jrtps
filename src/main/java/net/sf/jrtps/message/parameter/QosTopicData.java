package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosTopicData extends Parameter implements DataReaderPolicy<QosTopicData>, 
    DataWriterPolicy<QosTopicData>, TopicPolicy<QosTopicData> {
    
    private byte[] topicData;

    public QosTopicData(byte[] topicData) {
        super(ParameterEnum.PID_TOPIC_DATA);
        if (topicData == null) {
            this.topicData = new byte[0];
        }
        else {
            this.topicData = topicData;
        }
    }

    QosTopicData() {
        super(ParameterEnum.PID_TOPIC_DATA);
    }

    /**
     * Gets the topic data
     * @return topic data
     */
    public byte[] getTopicData() {
        return topicData;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        int len = bb.read_long();
        this.topicData = new byte[len];
        bb.read(topicData);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(topicData.length);
        bb.write(topicData);
    }

    @Override
    public boolean isCompatible(QosTopicData other) {
        return true; // Always true
    }

    public static QosTopicData defaultTopicData() {
        return new QosTopicData(new byte[0]);
    }
    
    public String toString() {
        return QosTopicData.class.getSimpleName() + "(" + Arrays.toString(topicData) + ")";
    }
}