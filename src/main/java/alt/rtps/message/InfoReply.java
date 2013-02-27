package alt.rtps.message;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Locator_t;

/**
 * This message is sent from an RTPS Reader to an RTPS Writer. It contains explicit information on where 
 * to send a reply to the Submessages that follow it within the same message.
 * 
 * @author mcr70
 * @see 9.4.5.9 InfoReply Submessage, 8.3.7.8 InfoReply
 */
public class InfoReply extends SubMessage {
	public static final int KIND = 0x0f;
	
	/**
	 * Indicates an alternative set of unicast addresses that the Writer
	 * should use to reach the Readers when replying to the Submessages that follow.
	 */
	private List<Locator_t> unicastLocatorList = new LinkedList<Locator_t>();
	/**
	 * Indicates an alternative set of multicast addresses that the Writer
	 * should use to reach the Readers when replying to the Submessages that follow.
	 * Only present when the MulticastFlag is set.
	 */
	private List<Locator_t> multicastLocatorList = new LinkedList<Locator_t>();
	
	public InfoReply(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}

	/**
	 * Returns the MulticastFlag. If true, message contains MulticastLocatorList
	 * @return
	 */
	public boolean multicastFlag() {
		return (header.flags & 0x2) != 0;
	}

	public List<Locator_t> getUnicastLocatorList() {
		return unicastLocatorList;
	}
	
	public List<Locator_t> getMulticastLocatorList() {
		return multicastLocatorList;
	}

	
	private void readMessage(RTPSByteBuffer bb) {
		long numLocators = bb.read_long(); // ulong
		for (int i = 0; i < numLocators; i++) {
			Locator_t loc = new Locator_t(bb);
			
			unicastLocatorList.add(loc);
		}
		
		if (multicastFlag()) {
			numLocators = bb.read_long(); // ulong
			for (int i = 0; i < numLocators; i++) {
				Locator_t loc = new Locator_t(bb);
				
				multicastLocatorList.add(loc);
			}			
		}
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(unicastLocatorList.size());
		for (Locator_t loc : unicastLocatorList) {
			loc.writeTo(buffer);
		}
		
		if (multicastFlag()) {
			buffer.write_long(multicastLocatorList.size());
			for (Locator_t loc : multicastLocatorList) {
				loc.writeTo(buffer);
			}			
		}
	}
}
