package net.sf.jrtps.udds.security;

import java.util.HashMap;

import net.sf.jrtps.Configuration;

/**
 * PluginFactory is used to instantiate different security plugin implementations.
 * uDDS provides two predefined plugin factories with names 'jks' and 'no-op'.
 * 'jks' plugin factory is used to create AuthenticationPlugins that load 
 * principal data from java keystore (JKS).
 * 'no-op' factory provides AuthenticationPlugins that succeed every time.  
 * In practice, this disables security features of uDDS.
 * 
 * @author mcr70
 */
public abstract class PluginFactory {
	public static final String KEYSTORE_PLUGIN_NAME = "jks";
	public static final String NO_OP_PLUGIN_NAME = "no-op";
	
	private static final HashMap<String,PluginFactory> pluginFactories = new HashMap<>();
	
	static {
		registerPluginFactory(KEYSTORE_PLUGIN_NAME, new KeystorePluginFactory());
		registerPluginFactory(NO_OP_PLUGIN_NAME, new NoOpPluginFactory());
	}
	
	/**
	 * Registers a PluginFactory. 
	 * @param name Name of the plugin 
	 * @param factory implementation of the PluginFactory
	 */
	public static void registerPluginFactory(String name, PluginFactory factory) {
		synchronized (pluginFactories) {
			pluginFactories.put(name, factory);
		}
	}
	
	/**
	 * Gets an instance of PluginFactory registered with given name
	 * @param name name of the plugin
	 * @return PluginFactory
	 * @throws PluginException if there was not PluginFactory registered with given name
	 */
	public static PluginFactory getInstance(String name) throws PluginException {
		PluginFactory pluginFactory = pluginFactories.get(name);
		if (pluginFactory == null) {
			throw new PluginException("Could not find PluginFactory with name " + name);
		}
		
		return pluginFactory;
	}
	
	/**
	 * Creates an instance of AuthenticationPlugin
	 * @param conf
	 * @return AuthenticationPlugin
	 * @throws PluginException 
	 */
	public abstract AuthenticationPlugin createAuthenticationPlugin(Configuration conf) throws PluginException;
}
