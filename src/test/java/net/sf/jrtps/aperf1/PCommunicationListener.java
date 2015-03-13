package net.sf.jrtps.aperf1;

import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.udds.CommunicationListener;

public class PCommunicationListener implements CommunicationListener<SubscriptionData> {

	private CountDownLatch cdl;

	public PCommunicationListener(CountDownLatch cdl) {
		this.cdl = cdl;
	}

	@Override
	public void deadlineMissed(KeyHash instanceKey) {
	}

	@Override
	public void entityMatched(SubscriptionData ed) {
		cdl.countDown();
	}

	@Override
	public void inconsistentQoS(SubscriptionData ed) {
	}
}
