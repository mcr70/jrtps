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
	private final CryptoPlugin cryptoPlugin;
	private final Configuration conf;

	static {
		logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);
	}

	protected AuthenticationPlugin(Configuration conf) {
		this.conf = conf;
		this.cryptoPlugin = new CryptoPlugin(conf);
	}
	
	public Configuration getConfiguration() {
		return conf;
	}
	
	/**
	 * Gets the name of this plugin. Name of the plugin can be used in configuration files.
	 * @return Name of the plugin.
	 */
	public abstract String getName();
	
	/**
	 * Registers a AuthenticationPlugin. 
	 * @param plugin AuthenticationPlugin 
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
	 * @throws RuntimeException if there was not AuthenticationPlugin registered with given name
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
	 * This method is called after security endpoints have been created by participant.
	 * @param p Participant
	 */
	public void init(Participant p) {
	}

	
	/**
	 * Begins a handshake protocol with given ParticipantData.
	 * @param pd ParticipantData
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
	 * @param aListener AuthenticationListener
	 */
	public void addAuthenticationListener(AuthenticationListener aListener) {
		authListeners.add(aListener);
	}

	/**
	 * Removes an AuthenticationListener.
	 * @param aListener AuthenticationListener
	 */
	public void removeAuthenticationListener(AuthenticationListener aListener) {
		authListeners.remove(aListener);
	}

	/**
	 * Notifies AuthenticationListeners of successful authentication
	 * @param ad AuthenticationData
	 */
	protected void notifyListenersOfSuccess(AuthenticationData ad) {
		if (ad.getSharedSecret() != null) {
			cryptoPlugin.setParticipantKeyMaterial(getGuid().getPrefix(), ad.getParticipantData().getGuidPrefix(), ad.getSharedSecret());
		}
			
		for (AuthenticationListener al : authListeners) {
			al.authenticationSucceeded(ad.getParticipantData());
		}
	}

	/**
	 * Notifies AuthenticationListeners of failed authentication
	 * @param pd ParticipantData
	 */
	protected void notifyListenersOfFailure(ParticipantData pd) {
		for (AuthenticationListener al : authListeners) {
			al.authenticationFailed(pd);
		}
	}

	/**
	 * Gets the Guid of local participant
	 * @return Guid Guid
	 */
	public abstract Guid getGuid();

	/**
	 * Gets CryptoPlugin associated with this AuthenticationPlugin
	 * @return CryptoPlugin
	 */
	public CryptoPlugin getCryptoPlugin() {
		return cryptoPlugin;
	}
}
