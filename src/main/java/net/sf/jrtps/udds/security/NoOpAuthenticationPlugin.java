package net.sf.jrtps.udds.security;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;

/**
 * AuthenticationPlugin that always successfully authenticates
 * 
 * @author mcr70
 */
public class NoOpAuthenticationPlugin extends AuthenticationPlugin {

	@Override
	public void beginHandshake(ParticipantData pd) {
		super.notifyListenersOfSuccess(pd);
	}

	@Override
	public IdentityToken getIdentityToken() {
		return null;
	}
}
