package alt.rtps.message.data;

import alt.rtps.message.DataEncapsulation;
import alt.rtps.transport.RTPSByteBuffer;

public class CDREncapsulation extends DataEncapsulation {

	private final RTPSByteBuffer bb;
	private final short options;


	public CDREncapsulation(RTPSByteBuffer bb, short options) {
		this.bb = bb;
		this.options = options;
	}
	

	@Override
	public boolean containsData() {
		return true;
	}

	@Override
	public byte[] getSerializedPayload() {
		return null;
	}

}
