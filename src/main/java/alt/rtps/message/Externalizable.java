package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;

public interface Externalizable {
	public void readFrom(RTPSByteBuffer buffer);
	public void writeTo(RTPSByteBuffer buffer);
}
