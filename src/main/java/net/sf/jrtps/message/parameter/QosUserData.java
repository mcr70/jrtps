package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosUserData extends Parameter implements DataReaderPolicy<QosUserData>, DataWriterPolicy<QosUserData> {
	private byte[] userData;

	public QosUserData(byte[] userData) {
		super(ParameterId.PID_USER_DATA);
        if (userData == null) {
            this.userData = new byte[0];
        }
        else {
            this.userData = userData;
        }
	}
	
	QosUserData() {
        super(ParameterId.PID_USER_DATA);
    }

	/**
	 * Gets the user data
	 * @return user data
	 */
	public byte[] getUserData() {
	    return userData;
	}
	
    @Override
    public void read(RTPSByteBuffer bb, int length) {
    	int len = bb.read_long();
    	this.userData = new byte[len];
    	bb.read(userData);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
    	bb.write_long(userData.length);
    	bb.write(userData);
    }

    @Override
    public boolean isCompatible(QosUserData other) {
        return true; // Always true
    }

    public static QosUserData defaultUserData() {
        return new QosUserData(new byte[0]);
    }
    
    public String toString() {
        return QosUserData.class.getSimpleName() + "(" + Arrays.toString(userData) + ")";
    }
}