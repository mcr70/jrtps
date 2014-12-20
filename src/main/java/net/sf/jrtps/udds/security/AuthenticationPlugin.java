package net.sf.jrtps.udds.security;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.Participant;

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

	private final Set<AuthenticationListener> authListeners = new CopyOnWriteArraySet<>();

	
	public abstract void init(Participant p);

	
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


	public abstract Guid getGuid();
}
