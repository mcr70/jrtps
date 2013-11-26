package net.sf.jrtps;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.message.parameter.QosReliability;

/**
 * ReaderProxy.
 *
 * @author mcr70
 */
class ReaderProxy {
	private boolean expectsInlineQoS = false;
	private final ReaderData rd;
	private long readersHighestSeqNum = 0;
	
	
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

	public long getReadersHighestSeqNum() {
		return readersHighestSeqNum;
	}

	public void setReadersHighestSeqNum(long l) {
		this.readersHighestSeqNum = l;
	}
}
