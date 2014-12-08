package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
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

	SecureRandom random = new SecureRandom();
	// Latches used to wait for remote participants
	private final Map<IdentityToken, CountDownLatch> handshakeLatches = new HashMap<>();

	private final KeyStore ks;
	private final Certificate ca;
	private final Signature signature = Signature.getInstance("SHA1withDSA"); // TODO: hardcoded
	
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

		this.ks = KeyStore.getInstance("JKS");

		InputStream is = getClass().getResourceAsStream("/jrtps.jks");
		String pwd = conf.getKeystorePassword();

		ks.load(is, pwd.toCharArray());
		
		this.ca = ks.getCertificate(conf.getSecurityCA());
		if (ca == null) {
			throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
		}

		String alias = conf.getSecurityPrincipal();

		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		if (cert == null) {
			throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
		}

		verify(cert);
		
		Key privateKey = ks.getKey(alias, conf.getSecurityPrincipalPassword().toCharArray());
		IdentityCredential identityCreadential = new IdentityCredential(cert, privateKey);

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

		HandshakeRequestMessageToken hrmt = 
				new HandshakeRequestMessageToken(getLocalIdentity().getIdentityCredential(),
						createChallenge()); 

		ParticipantStatelessMessage psm = 
				new ParticipantStatelessMessage(statelessWriter.getGuid(),
						new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
						hrmt);

		logger.debug("Sending handshake request to {}", remoteGuid.getPrefix());
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

	/**
	 * Begins a handshake protocol.
	 * See 9.3.4.2 Protocol description
	 * 
	 * @param pd
	 * @return
	 */
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


	private byte[] sign(byte[] challenge) throws InvalidKeyException, SignatureException {
		byte[] signatureBytes = null;
		
		synchronized (signature) {
			signature.initSign(identity.getIdentityCredential().getPrivateKey());
			signature.update(challenge);
			signatureBytes = signature.sign();
		}
		
		return signatureBytes;
	}

	private boolean verify(byte[] signedBytes, PublicKey privateKey) {
		return false;
	}

	private void verify(X509Certificate certificate) throws CertificateException {
		try {
			certificate.verify(ca.getPublicKey());
		} catch (InvalidKeyException /* | CertificateException */ 
				| NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			// TODO: what should we throw here
			throw new CertificateException(e);
		}
	}

	private byte[] createChallenge() {
		String s = "CHALLENGE:" + new BigInteger(96, random);
		return s.getBytes();
	}

	private byte[] createSharedSecret() {
		byte[] sharedSecret = new byte[20]; // TODO: hardcoded
		random.nextBytes(sharedSecret);
		
		return sharedSecret;
	}

	void doHandshake(ParticipantStatelessMessage psm) {
		if (psm.message_data != null && psm.message_data.length > 0) {
			String classId = psm.message_data[0].class_id;
			
			if (HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH.equals(classId)) {			
				doHandshake(psm.message_identity, (HandshakeRequestMessageToken) psm.message_data[0],
						psm.source_endpoint_key);
			}
			else if (HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_DSA_DH.equals(classId)) {
				doHandshake(psm.related_message_identity, 
						(HandshakeReplyMessageToken) psm.message_data[0]);
				
			}
			else if (HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_DSA_DH.equals(classId)) {
				doHandshake((HandshakeFinalMessageToken) psm.message_data[0]);					
			}
			else {
				logger.warn("HandshakeMessageToken with class_id '{}' not handled", classId);
			}
		}
		else {
			logger.warn("Missing message_data from {}", psm.source_endpoint_key);
		}
	}


	private void doHandshake(MessageIdentity messageIdentity, HandshakeRequestMessageToken hReq,
			Guid remoteTarget) {
		logger.debug("doHandshake(request)");
		X509Certificate certificate = hReq.getCertificate();
		try {
			verify(certificate);
		} catch (CertificateException e) {
			logger.warn("Failed to verify certificate", e);
			// TODO: cancel handshake
		}
		
		byte[] challenge = hReq.getChallenge();
		byte[] signedChallenge = null;
		try {
			signedChallenge = sign(challenge);
		} catch (InvalidKeyException | SignatureException e) {
			logger.warn("Failed to sign challenge", e);
			// TODO: cancel handshake
		}
		
		byte[] challengeBytes = createChallenge();
		
		HandshakeReplyMessageToken hrmt = 
				new HandshakeReplyMessageToken(identity.getIdentityCredential(),
						signedChallenge, challengeBytes);

		ParticipantStatelessMessage psm = 
				new ParticipantStatelessMessage(statelessWriter.getGuid(),
						new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
						hrmt);

		logger.debug("Sending handshake reply to {}", remoteTarget.getPrefix());
		statelessWriter.write(psm);
	}

	private void doHandshake(MessageIdentity relatedMessageIdentity, HandshakeReplyMessageToken hRep) {
		logger.debug("doHandshake(reply)");
		
		X509Certificate certificate = hRep.getCertificate();
		try {
			verify(certificate);
		} catch (CertificateException e) {
			logger.warn("Failed to verify certificate", e);
			// TODO: cancel handshake
		}
		
		byte[] signedChallenge = hRep.getSignedChallenge();
		verify(signedChallenge, certificate.getPublicKey());
		
		byte[] sharedSecret = createSharedSecret();
		byte[] encryptedSharedSecret = encrypt(certificate.getPublicKey(), sharedSecret);
	}

	private byte[] encrypt(PublicKey publicKey, byte[] sharedSecret) {
		// TODO Auto-generated method stub
		return null;
	}


	private void doHandshake(HandshakeFinalMessageToken hFin) {
		logger.debug("doHandshake(final)");
		// TODO Auto-generated method stub
	}
}
