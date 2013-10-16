package net.sf.jrtps.message;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Locator_t;

/**
 * This message is sent from an RTPS Reader to an RTPS Writer. It contains explicit information on where 
 * to send a reply to the Submessages that follow it within the same message.
 * 
 * @author mcr70
 * @see 9.4.5.9 InfoReply Submessage, 8.3.7.8 InfoReply
 */
public class InfoReply extends SubMessage {
	public static final int KIND = 0x0f;
	
	private List<Locator_t> unicastLocatorList = new LinkedList<Locator_t>();
	private List<Locator_t> multicastLocatorList = new LinkedList<Locator_t>();

	public InfoReply(List<Locator_t> unicastLocators, List<Locator_t>multicastLocators) {
		super(new SubMessageHeader(KIND));
		
		this.unicastLocatorList = unicastLocators;
		this.multicastLocatorList = multicastLocators;
		
		if (multicastLocatorList != null || multicastLocatorList.size() > 0) {
			header.flags |= 0x2;
		}
	}
	
	InfoReply(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
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

	/**
	 * Returns the MulticastFlag. If true, message contains MulticastLocatorList
	 * @return
	 */
	public boolean multicastFlag() {
		return (header.flags & 0x2) != 0;
	}

	/**
	 * Indicates an alternative set of unicast addresses that the Writer
	 * should use to reach the Readers when replying to the Submessages that follow.
	 */
	public List<Locator_t> getUnicastLocatorList() {
		return unicastLocatorList;
	}
	
	/**
	 * Indicates an alternative set of multicast addresses that the Writer
	 * should use to reach the Readers when replying to the Submessages that follow.
	 * Only present when the MulticastFlag is set.
	 */
	public List<Locator_t> getMulticastLocatorList() {
		return multicastLocatorList;
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
	
	public String toString() {
		return super.toString() + ", " + unicastLocatorList + ", " + multicastLocatorList;
	}
}
