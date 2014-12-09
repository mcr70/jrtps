package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyStoreAuthenticationPlugin is an Authentication plugin as discussed in
 * DDS Security specification. Chapter 9.3.3 <i>DDS:Auth:PKI-RSA/DSA-DH plugin behavior</i>
 * describes the plugin behavior.
 * 
 * @author mcr70
 */
public class KeyStoreAuthenticationPlugin {
	public static final String AUTH_LOG_CATEGORY = "dds.sec.auth";

	private static Logger logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);

	private HashMap<GuidPrefix, AuthenticationData> authDataMap = new HashMap<>();

	SecureRandom random = new SecureRandom();
	// Latches used to wait for remote participants
	private final Map<IdentityToken, CountDownLatch> handshakeLatches = new HashMap<>();

	private final KeyStore ks;
	private final Certificate ca;
	private final Signature signature = Signature.getInstance("SHA256withRSA"); // TODO: hardcoded

	private final Configuration conf;
	private final DataWriter<ParticipantStatelessMessage> statelessWriter;
	private final DataReader<ParticipantStatelessMessage> statelessReader;

	private final LocalIdentity identity;
	private final Participant participant;
	private final Cipher cipher;

	private volatile long psmSequenceNumber = 1; // ParticipantStatelessMessage sequence number


	public KeyStoreAuthenticationPlugin(Participant p1, Configuration conf, Guid originalGuid) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, UnrecoverableKeyException, NoSuchPaddingException {
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

		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // TODO: hardcoded

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


	LocalIdentity getLocalIdentity() {
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
			AuthenticationData authData = new AuthenticationData();
			authDataMap.put(pd.getGuidPrefix(), authData);
			
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


	private void beginHandshakeRequest(IdentityToken remoteIdentity, Guid remoteGuid) {
		byte[] challenge = createChallenge();
		
		AuthenticationData authData = authDataMap.get(remoteGuid.getPrefix());
		authData.setRequestChallenge(challenge);
		
		HandshakeRequestMessageToken hrmt = 
				new HandshakeRequestMessageToken(getLocalIdentity().getIdentityCredential(),
						challenge); 

		ParticipantStatelessMessage psm = 
				new ParticipantStatelessMessage(new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
						hrmt);

		logger.debug("Sending handshake request to {}", remoteGuid.getPrefix());
		statelessWriter.write(psm);
	}



	void doHandshake(ParticipantStatelessMessage psm) {
		if (psm.message_data != null && psm.message_data.length > 0) {
			String classId = psm.message_data[0].class_id;

			if (HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH.equals(classId)) {			
				doHandshake(psm.message_identity, (HandshakeRequestMessageToken) psm.message_data[0],
						psm.source_endpoint_key);
			}
			else if (HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_DSA_DH.equals(classId)) {
				doHandshake(psm.getSourceGuid(), psm.related_message_identity, 
						(HandshakeReplyMessageToken) psm.message_data[0]);

			}
			else if (HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_DSA_DH.equals(classId)) {
				doHandshake(psm.message_identity, (HandshakeFinalMessageToken) psm.message_data[0]);					
			}
			else {
				logger.warn("HandshakeMessageToken with class_id '{}' not handled", classId);
			}
		}
		else {
			logger.warn("Missing message_data from {}", psm.source_endpoint_key);
		}
	}


	private void doHandshake(MessageIdentity mi, HandshakeRequestMessageToken hReq,
			Guid remoteTarget) {
		logger.debug("doHandshake(request)");
		X509Certificate certificate = hReq.getCertificate();
		
		try {
			verify(certificate);
			
			AuthenticationData authData = new AuthenticationData(certificate);
			//authData.setCertificate(certificate);
			authDataMap.put(mi.getSourceGuid().getPrefix(), authData);
			
			byte[] challenge = hReq.getChallenge();
			authData.setRequestChallenge(challenge);
			
			byte[] signedChallenge = sign(challenge);

			byte[] challengeBytes = createChallenge();
			authData.setReplyChallenge(challengeBytes);
			
			HandshakeReplyMessageToken hrmt = 
					new HandshakeReplyMessageToken(identity.getIdentityCredential(),
							signedChallenge, challengeBytes);

			ParticipantStatelessMessage psm = 
					new ParticipantStatelessMessage(new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
							hrmt);

			logger.debug("Sending handshake reply to {}", remoteTarget.getPrefix());
			statelessWriter.write(psm);
		}
		catch(CertificateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException 
				| NoSuchProviderException e) {
			logger.warn("Failed to process handshake request message token");
			// TODO: cancel handshake
		}
	}

	private void doHandshake(Guid sourceGuid, MessageIdentity relatedMessageIdentity, HandshakeReplyMessageToken hRep) {
		logger.debug("doHandshake(reply)");

		X509Certificate certificate = hRep.getCertificate();
		try {
			AuthenticationData authData = authDataMap.get(sourceGuid.getPrefix());
			verify(certificate);
			authData.setCertificate(certificate);
			
			byte[] signedChallenge = hRep.getSignedChallenge();
			verify(signedChallenge, certificate.getPublicKey());

			byte[] sharedSecret = createSharedSecret();
			byte[] encryptedSharedSecret = encrypt(certificate.getPublicKey(), sharedSecret);
			byte[] challenge = hRep.getChallenge();
			authData.setReplyChallenge(challenge);

			byte[] hashedData = hash(concatenate(challenge, encryptedSharedSecret));
			byte[] signedData = sign(hashedData);

			HandshakeFinalMessageToken hfmt = 
					new HandshakeFinalMessageToken(identity.getIdentityCredential(),
							encryptedSharedSecret, signedData);

			ParticipantStatelessMessage psm = 
					new ParticipantStatelessMessage(new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
							hfmt);

			authData.setSharedSecret(sharedSecret);
			
			logger.info("Authenticated {} successfully", authData.getCertificate().getSubjectDN());
			
			logger.debug("Sending handshake final message");
			statelessWriter.write(psm);
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException | SignatureException 
				| CertificateException | NoSuchProviderException e) {
			logger.warn("Failed to process handshake reply message token", e);
			// TODO: cancel handshake
		}
	}


	private void doHandshake(MessageIdentity mi, HandshakeFinalMessageToken hFin) {
		logger.debug("doHandshake(final)");

		AuthenticationData authData = authDataMap.get(mi.getSourceGuid().getPrefix());
		X509Certificate cert = authData.getCertificate();
		if (cert == null) {
			logger.warn("Could not find certificate for {}", mi.getSourceGuid());
			// TODO: cancel handshake
		}

		byte[] signedData = hFin.getSignedData();
		try {
			verify(signedData, cert.getPublicKey());
			byte[] encryptedSharedSecret = hFin.getEncryptedSharedSicret();
			byte[] sharedSecret = decrypt(encryptedSharedSecret);
			
			authData.setSharedSecret(sharedSecret);
			logger.info("Authenticated {} succesfully", authData.getCertificate().getSubjectDN());
		} catch (InvalidKeyException | SignatureException | IllegalBlockSizeException | BadPaddingException e) {
			logger.warn("Failed to process handshake final message token", e);
			// TODO: cancel handshake
		}
	}


	private byte[] sign(byte[] bytesToSign) throws InvalidKeyException, SignatureException {
		byte[] signatureBytes = null;

		synchronized(signature) {
			signature.initSign(identity.getIdentityCredential().getPrivateKey());
			signature.update(bytesToSign);
			signatureBytes = signature.sign();
		}

		return signatureBytes;
	}

	private boolean verify(byte[] signedBytes, PublicKey publicKey) throws InvalidKeyException, SignatureException {
		synchronized(signature) {
			signature.initVerify(publicKey);
			return signature.verify(signedBytes);
		}
	}

	private void verify(X509Certificate certificate) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		certificate.verify(ca.getPublicKey());
	}

	private byte[] hash(byte[] bytesToHash) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(bytesToHash);
	}

	private byte[] encrypt(PublicKey publicKey, byte[] bytesToEncrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		synchronized (cipher) {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(bytesToEncrypt);
		}
	}

	private byte[] decrypt(byte[] bytesToDecrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		synchronized (cipher) {
			cipher.init(Cipher.DECRYPT_MODE, identity.getIdentityCredential().getPrivateKey());
			return cipher.doFinal(bytesToDecrypt);
		}
	}


	private byte[] concatenate(byte[] bytes1, byte[] bytes2) {
		byte[] newBytes = new byte[bytes1.length + bytes2.length];

		System.arraycopy(bytes1, 0, newBytes, 0, bytes1.length);
		System.arraycopy(bytes2, 0, newBytes, bytes1.length, bytes2.length);

		return newBytes;
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
}
