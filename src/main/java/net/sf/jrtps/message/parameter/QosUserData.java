package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosUserData extends Parameter implements DataReaderPolicy<QosUserData>, DataWriterPolicy<QosUserData> {
	private byte[] userData;

	public QosUserData(byte[] userData) {
		super(ParameterEnum.PID_USER_DATA);
        if (userData == null) {
        	throw new NullPointerException("userData cannot be null");
        }
		
		this.userData = userData;
	}
	
	QosUserData() {
        super(ParameterEnum.PID_USER_DATA);
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
}