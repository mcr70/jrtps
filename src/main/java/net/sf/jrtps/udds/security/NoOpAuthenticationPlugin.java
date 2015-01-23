package net.sf.jrtps.udds.security;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationPlugin that always successfully authenticates
 * 
 * @author mcr70
 */
public class NoOpAuthenticationPlugin extends AuthenticationPlugin {
	private static Logger logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);

	public static final String PLUGIN_NAME = "none";
	private static final Random random = new Random(System.currentTimeMillis());

	public NoOpAuthenticationPlugin(Configuration conf) {
		super(conf);
	}
	
	@Override
	public void beginHandshake(ParticipantData pd) {
		AuthenticationData ad = new AuthenticationData(pd);
		String noOpSharedSecret = getConfiguration().getNoOpSharedSecret();
		try {
			ad.setSharedSecret(noOpSharedSecret.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to convert string {} to UTF-8", new Object[] {noOpSharedSecret}, e);
		}
		
		super.notifyListenersOfSuccess(ad);
	}

	@Override
	public IdentityToken getIdentityToken() {
		return null; // return null; do not advertise IdentityToken to remote participant
	}

	@Override
	public Guid getGuid() {
		byte[] prefix = new byte[12];
		random.nextBytes(prefix);
		
		return new Guid(new GuidPrefix(prefix), EntityId.PARTICIPANT);
	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}
}
