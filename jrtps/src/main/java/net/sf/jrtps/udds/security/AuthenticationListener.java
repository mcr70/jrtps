package net.sf.jrtps.udds.security;

import net.sf.jrtps.builtin.ParticipantData;

/**
 * AuthenticationListener
 * @author mcr70
 */
public interface AuthenticationListener {
	/**
	 * This method is called after successful authentication. 
	 * @param pd ParticipantData
	 */
	void authenticationSucceeded(ParticipantData pd);
	/**
	 * This method is called when authentication has failed. 
	 * @param pd ParticipantData
	 */
	void authenticationFailed(ParticipantData pd);
}
