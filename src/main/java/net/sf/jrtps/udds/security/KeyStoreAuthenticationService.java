package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyStoreAuthentication is an Authentication plugin as discussed in
 * DDS Security specification. Chapter 9.3.3 <i>DDS:Auth:PKI-RSA/DSA-DH plugin behavior</i>
 * describes the plugin behavior.
 * 
 * @author mcr70
 */
public class KeyStoreAuthenticationService {
	public static final String AUTH_LOG_CATEGORY = "dds.sec.auth";
	
	private static Logger logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);

	// Latches used to wait for remote participants
	private final Map<IdentityToken, CountDownLatch> handshakeLatches = new HashMap<>();

	private final KeyStore ks;

	private final Configuration conf;
	private final DataWriter<ParticipantStatelessMessage> statelessWriter;
	private final DataReader<ParticipantStatelessMessage> statelessReader;

	private final LocalIdentity identity;
	private final Participant participant;
	private volatile long psmSequenceNumber = 1; // ParticipantStatelessMessage sequence number

	public KeyStoreAuthenticationService(Participant p1, Configuration conf, Guid originalGuid) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, UnrecoverableKeyException {
		this.participant = p1;
		this.statelessWriter = 
				(DataWriter<ParticipantStatelessMessage>) p1.getWriter(EntityId.BUILTIN_PARTICIPANT_STATELESS_WRITER);
		this.statelessReader = 
				(DataReader<ParticipantStatelessMessage>) p1.getReader(EntityId.BUILTIN_PARTICIPANT_STATELESS_READER);
		this.statelessReader.addSampleListener(new ParticipantStatelessMessageListener(participant, this));

		this.conf = conf;

		ks = KeyStore.getInstance("JKS");

		InputStream is = getClass().getResourceAsStream("/jrtps.jks");
		String pwd = conf.getKeystorePassword();

		ks.load(is, pwd.toCharArray());

		Certificate ca = ks.getCertificate(conf.getSecurityCA());
		if (ca == null) {
			throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
		}

		String alias = conf.getSecurityPrincipal();

		X509Certificate principal = (X509Certificate) ks.getCertificate(alias);
		if (principal == null) {
			throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
		}

		principal.verify(ca.getPublicKey());

		IdentityCredential identityCreadential = new IdentityCredential(principal, ks.getKey(alias, conf.getSecurityPrincipalPassword().toCharArray()));

		identity = new LocalIdentity(originalGuid, identityCreadential);

		logger.debug("Succesfully locally authenticated {}", conf.getSecurityPrincipal());
	}


	public X509Certificate getCertificate() {
		return identity.getIdentityCredential().getCertificate();
	}

	public Guid getOriginalGuid() {
		return identity.getOriginalGuid();
	}


	/**
	 * This method is called when a remote participant has been detected and it has 
	 * DDS security capabilities by providing IdentityToken with ParticipantBuiltinTopicData.
	 * 
	 * See ch. 7.4.1.3 of DDS Security specification: Extension to RTPS Standard DCPSParticipants 
	 * Builtin Topic, and ch. 9.3.3 DDS:Auth:PKI-RSA/DSA-DH plugin behavior
	 * 
	 * @param remoteIdentity IdentityToken of remote participant
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateEncodingException 
	 */
	public void validateRemoteIdentity(IdentityToken remoteIdentity, Guid remoteGuid) throws CertificateEncodingException, NoSuchAlgorithmException {
		int comparison = identity.getIdentityToken().getEncodedHash().compareTo(remoteIdentity.getEncodedHash());
		if (comparison < 0) { // Remote is lexicographically greater
			// VALIDATION_PENDING_HANDSHAKE_REQUEST
			beginHandshakeRequest(remoteIdentity, remoteGuid);
		}
		else if (comparison > 0) { // Remote is lexicographically smaller
			// VALIDATION_PENDING_HANDSHAKE_MESSAGE
			// Wait for remote entity to send handshake message
			CountDownLatch latch = handshakeLatches.remove(remoteIdentity);
			try {
				boolean await = latch.await(conf.getHandshakeTimeout(), TimeUnit.MILLISECONDS);
				handshakeLatches.remove(remoteIdentity);
				if (await) {
					beginHandshakeReply();
				}
				else {
					logger.warn("Failed to get handshake message from remote entity on time");
				}
			} catch (InterruptedException e) {
				handshakeLatches.remove(remoteIdentity);
				logger.warn("Interrupted. Returning from validateRemoteIdentity()");
				return;
			}
		}
		else { // Remote has the same identity as local
			// ???
		}
	}

	void beginHandshakeRequest(IdentityToken remoteIdentity, Guid remoteGuid) {
		logger.debug("beginHandshakeRequest()");

		HandshakeRequestMessageToken hrmt = 
				new HandshakeRequestMessageToken(getOriginalGuid(), remoteGuid, 
						getLocalIdentity().getIdentityCredential(), 
						null /* permission credential */);

		ParticipantStatelessMessage psm = 
				new ParticipantStatelessMessage(
						new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
						hrmt);

		statelessWriter.write(psm);
	}


	void beginHandshakeReply() {
		logger.debug("beginHandshakeReply()");
		// TODO Auto-generated method stub

	}


	public LocalIdentity getLocalIdentity() {
		return identity;
	}

	void cancelHandshake(IdentityToken iToken) {
		CountDownLatch latch = handshakeLatches.remove(iToken);
		latch.countDown(); // TODO: Is this correct way of canceling handshake
	}


	public IdentityToken getIdentityToken() {
		return getLocalIdentity().getIdentityToken();
	}


	public boolean beginHandshake(ParticipantData pd) {
		logger.debug("Begin handshake with {}", pd.getGuidPrefix());
		
		IdentityToken iToken = pd.getIdentityToken();
		if (iToken != null) {
			int comparison = identity.getIdentityToken().getEncodedHash().compareTo(iToken.getEncodedHash());		
			if (comparison < 0) { // Remote is lexicographically greater
				// VALIDATION_PENDING_HANDSHAKE_REQUEST
				beginHandshakeRequest(iToken, pd.getGuid());
			}
			else if (comparison > 0) {
				logger.debug("Starting to wait for HandshakeRequestMessage");
			}
			else {
				logger.debug("Remote identity is the same as we are");
			}
		}
		else {
			logger.debug("Failed to authenticate; No IdentityToken provided by {}", pd.getGuidPrefix());
		}
		
		return false;
	}


	public void doHandshake(MessageIdentity messageIdentity, HandshakeRequestMessageToken hReq) {
		// TODO Auto-generated method stub
	}


	public void doHandshake(MessageIdentity relatedMessageIdentity, HandshakeReplyMessageToken hRep) {
		// TODO Auto-generated method stub
	}


	public void doHandshake(HandshakeFinalMessageToken hFin) {
		// TODO Auto-generated method stub
	}
}
