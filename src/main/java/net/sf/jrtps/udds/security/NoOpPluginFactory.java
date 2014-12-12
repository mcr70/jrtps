package net.sf.jrtps.udds.security;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.udds.Participant;

class NoOpPluginFactory extends PluginFactory {

	@Override
	public AuthenticationPlugin createAuthenticationPlugin(Participant p,
			Configuration conf) throws PluginException {
		return new NoOpAuthenticationPlugin();
	}

}
