package net.sf.jrtps.udds.security;

import java.util.Random;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.udds.Participant;

/**
 * AuthenticationPlugin that always successfully authenticates
 * 
 * @author mcr70
 */
public class NoOpAuthenticationPlugin extends AuthenticationPlugin {
	private static final Random random = new Random(System.currentTimeMillis());

	private Guid guid;

	public NoOpAuthenticationPlugin() {
		byte[] prefix = new byte[12];
		random.nextBytes(prefix);
		
		this.guid = new Guid(new GuidPrefix(prefix), EntityId.PARTICIPANT);
	}
	
	@Override
	public void beginHandshake(ParticipantData pd) {
		super.notifyListenersOfSuccess(pd);
	}

	@Override
	public IdentityToken getIdentityToken() {
		return null; // return null; do not advertise IdentityToken to remote participant
	}

	@Override
	public void init(Participant p) {
		// Nothing to do
	}

	@Override
	public Guid getGuid() {
		return guid;
	}
}
