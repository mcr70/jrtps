package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * The purpose of this Submessage is to allow the introduction of any padding necessary to meet any 
 * desired memory alignment requirements. Its has no other meaning.
 * 
 * @author mcr70
 * @see 8.3.7.11 Pad
 */
public class Pad extends SubMessage {
	public static final int KIND = 0x01;
	
	private byte[] bytes;
	
	public Pad(SubMessageHeader smh, RTPSByteBuffer buffer) {
		super(smh);
		
		readMessage(buffer);
	}

	
	private void readMessage(RTPSByteBuffer bb) {
		this.bytes = new byte[header.submessageLength];
		bb.read(bytes);
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(bytes);
	}
}
