package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.LocatorUDPv4_t;

public class InfoReplyIp4 extends SubMessage {
	public static final int KIND = 0x0d;
	
	private LocatorUDPv4_t unicastLocator;
	private LocatorUDPv4_t multicastLocator;

	public InfoReplyIp4(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}

	/**
	 * Returns the MulticastFlag. If true, message contains MulticastLocator
	 * @return
	 */
	public boolean multicastFlag() {
		return (header.flags & 0x2) != 0;
	}

	public LocatorUDPv4_t getUnicastLocator() {
		return unicastLocator;
	}
	
	public LocatorUDPv4_t getMulticastcastLocator() {
		return multicastLocator;
	}

	
	private void readMessage(RTPSByteBuffer bb) {
		unicastLocator = new LocatorUDPv4_t(bb);
		
		if (multicastFlag()) {
			multicastLocator = new LocatorUDPv4_t(bb);
		}
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		unicastLocator.writeTo(buffer);
		multicastLocator.writeTo(buffer);
	}	
}
