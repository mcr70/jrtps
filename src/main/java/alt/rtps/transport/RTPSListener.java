package alt.rtps.transport;

import alt.rtps.message.Message;

/**
 * An interface that is called on message arrival.
 * 
 * @author mcr70
 *
 */
public interface RTPSListener {
	public void onRTPSMessage(Message m);
}
