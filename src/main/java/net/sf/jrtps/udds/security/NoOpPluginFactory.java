package net.sf.jrtps.udds.security;

import net.sf.jrtps.Configuration;

class NoOpPluginFactory extends PluginFactory {

	@Override
	public AuthenticationPlugin createAuthenticationPlugin(Configuration conf) throws PluginException {
		return new NoOpAuthenticationPlugin();
	}

}
