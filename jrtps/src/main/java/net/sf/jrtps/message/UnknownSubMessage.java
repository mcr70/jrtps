package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class UnknownSubMessage extends SubMessage {
	private byte[] bytes;
	
	public UnknownSubMessage(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}

	private void readMessage(RTPSByteBuffer bb) {
		bytes = new byte[header.submessageLength];
		bb.read(bytes);
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(bytes);
	}
}
