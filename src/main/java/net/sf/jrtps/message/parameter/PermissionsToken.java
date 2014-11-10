package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class PermissionsToken extends Parameter {

	PermissionsToken() {
		super(ParameterId.PID_PERMISSIONS_TOKEN);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		// TODO Auto-generated method stub
		
	}

}
