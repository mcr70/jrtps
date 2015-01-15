package net.sf.jrtps.udds.security;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.Participant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for authentication plugin. udds Participant (BuiltinParticipantDataListener)
 * calls beginHandshake(ParticipantData) method during discovery.
 * Implementation is expected to call either notifyListenersOfSuccess(ParticipantData)
 * or notifyListenersOfFailure(ParticipantData) 
 * 
 * @author mcr70
 */
public abstract class AuthenticationPlugin {		
	public static final String AUTH_LOG_CATEGORY = "dds.sec.auth";
	private static final Logger logger;	

	private static final HashMap<String,AuthenticationPlugin> authPlugins = new HashMap<>();

	private final Set<AuthenticationListener> authListeners = new CopyOnWriteArraySet<>();

	static {
		logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);
		
		registerPlugin(new NoOpAuthenticationPlugin());
	}

	/**
	 * Gets the name of this plugin. Name of the plugin can be used in configuration files.
	 * @return Name of the plugin.
	 */
	public abstract String getName();
	
	/**
	 * Registers a AuthenticationPlugin. 
	 * @param name Name of the plugin 
	 * @param plugin 
	 */
	public static void registerPlugin(AuthenticationPlugin plugin) {
		synchronized (authPlugins) {
			authPlugins.put(plugin.getName(), plugin);
		}
	}

	/**
	 * Gets an instance of AuthenticationPlugin registered with given name
	 * @param name name of the plugin. If name is null or empty string, it is considered
	 *        as "none".
	 * @return AuthenticationPlugin
	 * @throws SecurityException if there was not AuthenticationPlugin registered with given name
	 */
	public static AuthenticationPlugin getInstance(String name) {
		if (name == null || "".equals(name)) {
			name = NoOpAuthenticationPlugin.PLUGIN_NAME;
		}
		
		AuthenticationPlugin plugin = authPlugins.get(name);
		if (plugin == null) {
			throw new RuntimeException("Could not find AuthenticationPlugin with name " + name);
		}
		
		return plugin;
	}
	
	/**
	 * Initializes AuthenticationPlugin
	 * @param p
	 * @param conf
	 */
	public abstract void init(Participant p, Configuration conf);

	
	/**
	 * Begins a handshake protocol with given ParticipantData.
	 * @param pd
	 */
	public abstract void beginHandshake(ParticipantData pd);
	
	/**
	 * Gets an IdentityToken that will be send to remote participant
	 * during discovery. null value may be returned if implementing 
	 * AuthenticationPlugin does not support authentication protocol as specified
	 * in DDS security specification.
	 * 
	 * @return IdentityToken
	 */
	public abstract IdentityToken getIdentityToken();

	/**
	 * Adds an AuthenticationListener.
	 * @param aListener
	 */
	public void addAuthenticationListener(AuthenticationListener aListener) {
		authListeners.add(aListener);
	}

	/**
	 * Removes an AuthenticationListener.
	 * @param aListener
	 */
	public void removeAuthenticationListener(AuthenticationListener aListener) {
		authListeners.remove(aListener);
	}

	/**
	 * Notifies AuthenticationListeners of successful authentication
	 * @param pd
	 */
	protected void notifyListenersOfSuccess(ParticipantData pd) {
		for (AuthenticationListener al : authListeners) {
			al.authenticationSucceded(pd);
		}
	}

	/**
	 * Notifies AuthenticationListeners of failed authentication
	 * @param pd
	 */
	protected void notifyListenersOfFailure(ParticipantData pd) {
		for (AuthenticationListener al : authListeners) {
			al.authenticationFailed(pd);
		}
	}

	/**
	 * Gets the Guid of local participant
	 * @return Guid
	 */
	public abstract Guid getGuid();
}
