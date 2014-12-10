package net.sf.jrtps.udds.security;

import net.sf.jrtps.builtin.ParticipantData;

/**
 * AuthenticationListener
 * @author mcr70
 */
public interface AuthenticationListener {
	/**
	 * This method is called after successful authentication. 
	 * @param pd
	 */
	void authenticationSucceded(ParticipantData pd);
	/**
	 * This method is called when authentication has failed. 
	 * @param pd
	 */
	void authenticationFailed(ParticipantData pd);
}
