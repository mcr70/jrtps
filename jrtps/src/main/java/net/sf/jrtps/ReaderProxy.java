package net.sf.jrtps;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.message.parameter.QosReliability;

/**
 * ReaderProxy.
 *
 * @author mcr70
 */
class ReaderProxy {
	private final ReaderData rd;
	private boolean expectsInlineQoS = false;
	private long readersHighestSeqNum = 0;
	private boolean active = true;
	private long heartbeatSentTime = 0; // set to 0 after acknack
	
	ReaderProxy(ReaderData readerData) {
		this(readerData, false);
	}

	ReaderProxy(ReaderData rd, boolean expectsInlineQoS) {
		this.rd = rd;
		this.expectsInlineQoS = expectsInlineQoS;
	}


	/**
	 * Returnbs true if remote reader expects QoS to be sent inline with each Data submessage.
	 * @return true or false
	 */
	boolean expectsInlineQoS() {
		return expectsInlineQoS;
	}
	
	/**
	 * Return true, if remote reader represented by this ReaderProxy is configured to be reliable.
	 * 
	 * @return
	 */
	boolean isReliable() {
		QosReliability policy = (QosReliability) rd.getQualityOfService().getPolicy(QosReliability.class);

		return policy.getKind() == QosReliability.Kind.RELIABLE;
	}
	
	
	ReaderData getReaderData() {
		return rd;
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
}
