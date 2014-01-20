package net.sf.jrtps;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Guid;

/**
 * ReaderProxy represents a remote reader. 
 *
 * @author mcr70
 */
public class ReaderProxy {
	private final SubscriptionData readerData;
	private final boolean expectsInlineQoS;
	private long readersHighestSeqNum = 0;
	private boolean active = true;
	private long heartbeatSentTime = 0; // set to 0 after acknack
	
	ReaderProxy(SubscriptionData readerData) {
		this(readerData, false);
	}

	ReaderProxy(SubscriptionData rd, boolean expectsInlineQoS) {
		this.readerData = rd;
		this.expectsInlineQoS = expectsInlineQoS;
	}

	/**
	 * Gets the ReaderData associated with this ReaderProxy.
	 * @return ReaderData
	 */
	public SubscriptionData getSubscriptionData() {
		return readerData;
	}

	/**
	 * Gets the guid represented by this ReaderProxy
	 * @return Guid
	 */
	public Guid getGuid() {
		return readerData.getKey();
	}
	
	/**
	 * Returns true if remote reader expects QoS to be sent inline with each Data submessage.
	 * @return true or false
	 */
	boolean expectsInlineQoS() {
		return expectsInlineQoS;
	}
	
	/**
	 * Return true, if remote reader represented by this ReaderProxy is configured to be reliable.
	 * 
	 * @return true, if this ReaderProxy represents a reliable reader
	 */
	boolean isReliable() {
		QosReliability policy = (QosReliability) readerData.getQualityOfService().getPolicy(QosReliability.class);

		return policy.getKind() == QosReliability.Kind.RELIABLE;
	}
	
	long getReadersHighestSeqNum() {
		return readersHighestSeqNum;
	}

	void setReadersHighestSeqNum(long l) {
		this.readersHighestSeqNum = l;
	}

	boolean isActive() {
		return active;
	}

	
	void heartbeatSent() {
		if (heartbeatSentTime != 0) {
			this.heartbeatSentTime = System.currentTimeMillis();
		}
		else {
			active = false;
		}
	}
	
	void ackNackReceived() {
		this.heartbeatSentTime = 0;
		active = true;
	}

	public String toString() {
		return getGuid().toString();
	}
}
