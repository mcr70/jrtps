package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosGroupData extends Parameter implements SubscriberPolicy<QosGroupData>, 
PublisherPolicy<QosGroupData>, Changeable {
	private byte[] groupData;

	public QosGroupData(byte[] groupData) {
        super(ParameterId.PID_GROUP_DATA);
        if (groupData == null) {
            this.groupData = new byte[0];
        }
        else {
            this.groupData = groupData;
        }
    }
	
	QosGroupData() {
        super(ParameterId.PID_GROUP_DATA);
    }

	/**
	 * Get the group data
	 * @return group data
	 */
	public byte[] getGroupData() {
	    return groupData;
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

    public String toString() {
        return QosGroupData.class.getSimpleName() + "(" + Arrays.toString(groupData) + ")";
    }
}