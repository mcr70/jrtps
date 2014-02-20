package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosGroupData extends Parameter implements SubscriberPolicy<QosGroupData>, PublisherPolicy<QosGroupData> {
	private byte[] groupData;

	public QosGroupData(byte[] groupData) {
        super(ParameterEnum.PID_GROUP_DATA);
        if (groupData == null) {
        	throw new NullPointerException("groupData cannot be null");
        }
        
		this.groupData = groupData;
    }
	
	QosGroupData() {
        super(ParameterEnum.PID_GROUP_DATA);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
    	int len = bb.read_long();
    	this.groupData = new byte[len];
    	bb.read(groupData);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
       	bb.write_long(groupData.length);
    	bb.write(groupData);
     }

    @Override
    public boolean isCompatible(QosGroupData other) {
        return true; // Always true
    }

    public static QosGroupData defaultGroupData() {
        return new QosGroupData(new byte[0]);
    }
}