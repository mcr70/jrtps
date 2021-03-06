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
import net.sf.jrtps.udds.security.AuthenticationData.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystoreAuthenticationPlugin is an Authentication plugin as discussed in
 * DDS Security specification. Chapter 9.3.3 <i>DDS:Auth:PKI-RSA/DSA-DH plugin behavior</i>
 * describes the plugin behavior.
 * 
 * @author mcr70
 */
public class JKSAuthenticationPlugin extends AuthenticationPlugin {
	public static final String PLUGIN_NAME = "jks";

	public static final String JKS_KEYSTORE_KEY = "udds.security.jks.keystore";
	public static final String JKS_KEYSTORE_PASSWORD_KEY = "udds.security.jks.keystore.password";
	public static final String JKS_CA_KEY = "udds.security.jks.ca";
	public static final String JKS_PRINCIPAL_KEY = "udds.security.jks.principal";
	public static final String JKS_PRINCIPAL_PASSWORD_KEY = "udds.security.jks.principal.password";


	private static Logger logger = LoggerFactory.getLogger(AUTH_LOG_CATEGORY);

	private HashMap<GuidPrefix, AuthenticationData> authDataMap = new HashMap<>();

	SecureRandom random = new SecureRandom();

	private final KeyStore ks;
	private Certificate ca;
	private final Signature signature = Signature.getInstance("SHA256withRSA"); // TODO: hardcoded

	private DataWriter<ParticipantStatelessMessage> statelessWriter;

	private LocalIdentity identity;
	private final Cipher cipher;

	private volatile long psmSequenceNumber = 1; // ParticipantStatelessMessage sequence number

	private String caName;


	@SuppressWarnings("unchecked")
	public JKSAuthenticationPlugin(Configuration conf) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, UnrecoverableKeyException, NoSuchPaddingException {
		super(conf);
		this.ks = KeyStore.getInstance("JKS");
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // TODO: hardcoded

		InputStream is = getClass().getResourceAsStream(conf.getProperty(JKS_KEYSTORE_KEY));
		String pwd = conf.getProperty(JKS_KEYSTORE_PASSWORD_KEY);

		try {
			ks.load(is, pwd.toCharArray());

			this.caName = conf.getProperty(JKS_CA_KEY);
			this.ca = ks.getCertificate(caName);
			if (ca == null) {
				throw new KeyStoreException("Failed to get a certificate for alias '" + caName + "'");
			}

			String alias = conf.getProperty(JKS_PRINCIPAL_KEY);

			X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
			if (cert == null) {
				throw new KeyStoreException("Failed to get a certificate for alias '" + alias + "'");
			}

			verify(cert);

			Key privateKey = ks.getKey(alias, conf.getProperty(JKS_PRINCIPAL_PASSWORD_KEY).toCharArray());
			IdentityCredential identityCreadential = new IdentityCredential(cert, privateKey);
			this.identity = new LocalIdentity(identityCreadential);

			logger.debug("Successfully locally authenticated {}", alias);
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException | InvalidKeyException | NoSuchProviderException | SignatureException | UnrecoverableKeyException e) {
			throw new RuntimeException("Failed to initialize JKSAuthenticationPlugin", e);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public void init(Participant p) {
		this.statelessWriter = 
				(DataWriter<ParticipantStatelessMessage>) p.getWriter(EntityId.BUILTIN_PARTICIPANT_STATELESS_WRITER);

		DataReader<ParticipantStatelessMessage> statelessReader = 
				(DataReader<ParticipantStatelessMessage>) p.getReader(EntityId.BUILTIN_PARTICIPANT_STATELESS_READER);

		statelessReader.addSampleListener(new ParticipantStatelessMessageListener(p, this));		
	}

	/**
	 * Gets IdentityToken
	 * @return identityToken
	 */
	@Override
	public IdentityToken getIdentityToken() {
		return getLocalIdentity().getIdentityToken();
	}

	/**
	 * Begins a handshake protocol.
	 * See 9.3.4.2 Protocol description
	 * 
	 * @param pd ParticipantData to authenticate
	 */
	@Override
	public void beginHandshake(ParticipantData pd) {
		synchronized (authDataMap) {
			if (authDataMap.containsKey(pd.getGuidPrefix())) {
				logger.debug("Handshake already in started");
				return;
			}
			else {
				authDataMap.put(pd.getGuidPrefix(), new AuthenticationData(pd));
			}
		}

		logger.debug("Begin handshake with {}", getGuid().getPrefix(), pd.getGuidPrefix());


		IdentityToken iToken = pd.getIdentityToken();
		if (iToken != null) {

			int comparison = identity.getIdentityToken().getEncodedHash().compareTo(iToken.getEncodedHash());		
			if (comparison < 0) { // Remote is lexicographically greater
				// VALIDATION_PENDING_HANDSHAKE_REQUEST
				sendHandshakeRequest(iToken, pd.getGuid());
			}
			else if (comparison > 0) {
				logger.debug("Starting to wait for HandshakeRequestMessage from {}", pd.getGuidPrefix());
			}
			else {
				logger.info("{} Remote identity is the same as we are", getGuid().getPrefix());
				if(getGuid().compareTo(pd.getGuid()) < 0) {
					sendHandshakeRequest(iToken, pd.getGuid());
				}
			}
		}
		else {
			logger.debug("Failed to authenticate; No IdentityToken provided by {}", pd.getGuidPrefix());
		}
	}

	/**
	 * Creates and sends HandshakeRequestMessage to remote participant
	 * 
	 * @param remoteIdentity
	 * @param remoteGuid
	 */
	private void sendHandshakeRequest(IdentityToken remoteIdentity, Guid remoteGuid) {
//		System.out.println("REQ:" + getGuid().getPrefix() + " -> " + remoteGuid.getPrefix() + " / " +
//				getLocalIdentity().getIdentityCredential().getCertificate().getSubjectDN() + ", " +
//				getLocalIdentity().getIdentityCredential().getCertificate().getSerialNumber());

		byte[] challenge = createChallenge();
		AuthenticationData authData = authDataMap.get(remoteGuid.getPrefix());
		authData.setRequestChallenge(challenge);
		authData.setState(State.REQ_SENT);
		
		HandshakeRequestMessageToken hrmt = 
				new HandshakeRequestMessageToken(getLocalIdentity().getIdentityCredential(),
						challenge); 

		ParticipantStatelessMessage psm = 
				new ParticipantStatelessMessage(new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
						hrmt);

		logger.debug("Sending handshake request to {}", remoteGuid.getPrefix());
		statelessWriter.write(psm);
	}


	/**
	 * Called by ParticipantStatelessMessageListener on reception of
	 * ParticipantStatelessMessage
	 * 
	 * @param psm
	 * @see ParticipantStatelessMessageListener#onSamples(java.util.List)
	 */
	void doHandshake(ParticipantStatelessMessage psm) {
		if (psm.message_data != null && psm.message_data.length > 0) {
			String classId = psm.message_data[0].class_id;

			if (HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH.equals(classId)) {			
				sendHandshakeReply(psm.message_identity, (HandshakeRequestMessageToken) psm.message_data[0],
						psm.source_endpoint_key);
			}
			else if (HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_DSA_DH.equals(classId)) {
				sendHandshakeFinal(psm.getSourceGuid(), psm.related_message_identity, 
						(HandshakeReplyMessageToken) psm.message_data[0]);

			}
			else if (HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_DSA_DH.equals(classId)) {
				processHandshakeFinal(psm.message_identity, (HandshakeFinalMessageToken) psm.message_data[0]);					
			}
			else {
				logger.warn("HandshakeMessageToken with class_id '{}' not handled", classId);
			}
		}
		else {
			logger.warn("Missing message_data from {}", psm.source_endpoint_key);
		}
	}

	/**
	 * Called on the reception of handshake request message.
	 */
	private void sendHandshakeReply(MessageIdentity mi, HandshakeRequestMessageToken hReq,
			Guid remoteTarget) {
		logger.debug("doHandshake(request) from {}", mi.getSourceGuid());
		AuthenticationData authData = authDataMap.get(mi.getSourceGuid().getPrefix());

		synchronized (authData) {
			if (authData.getState() != null) {
				return;
			}
			authData.setState(State.REP_SENT);
		}

		//System.out.println("REP:" + getGuid().getPrefix() + " -> " + remoteTarget.getPrefix());
		
		try {
			X509Certificate certificate = hReq.getCertificate();
			verify(certificate);

			authData.setCertificate(certificate);

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
			logger.warn("Failed to process handshake request message token", e);

			cancelHandshake(authData.getParticipantData());
		}
	}

	/**
	 * Called on reception of handshake reply message
	 */
	private void sendHandshakeFinal(Guid sourceGuid, MessageIdentity relatedMessageIdentity, HandshakeReplyMessageToken hRep) {
		logger.debug("doHandshake(reply)");
		
		X509Certificate certificate = hRep.getCertificate();
		AuthenticationData authData = authDataMap.get(sourceGuid.getPrefix());

		synchronized (authData) {
			if (authData.getState() != State.REQ_SENT) {
				return;
			}
			authData.setState(State.FIN_SENT);
		}

//		System.out.println("FIN:" + getGuid().getPrefix() + " -> " + sourceGuid.getPrefix() + 
//				" / " + certificate.getSubjectDN() + ", " + certificate.getSerialNumber());
		
		try {
			verify(certificate);
			authData.setCertificate(certificate);

			byte[] signedChallenge = hRep.getSignedChallenge();
			verify(signedChallenge, certificate.getPublicKey());

			byte[] sharedSecret = createSharedSecret();
			//System.out.println(getGuid().getPrefix() + ", " + sourceGuid.getPrefix() + ", SHARED SECRET " + Arrays.toString(sharedSecret));
			// Register local key material also
			//getCryptoPlugin().setParticipantKeyMaterial(getLocalIdentity().getGuid().getPrefix(), sharedSecret);
			byte[] encryptedSharedSecret = encrypt(certificate.getPublicKey(), sharedSecret);
			//System.out.println(certificate.getSerialNumber() + ", " + Arrays.toString(encryptedSharedSecret));
			
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

			logger.debug("Sending handshake final message");
			statelessWriter.write(psm);

			logger.info("{} Authenticated {} successfully", getGuid().getPrefix(), authData.getCertificate().getSubjectDN());
			notifyListenersOfSuccess(authData);
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException | SignatureException 
				| CertificateException | NoSuchProviderException e) {
			logger.warn("Failed to process handshake reply message token", e);

			cancelHandshake(authData.getParticipantData());
		}
	}

	/**
	 * Called on reception of handshake final message
	 */
	private void processHandshakeFinal(MessageIdentity mi, HandshakeFinalMessageToken hFin) {
		logger.debug("doHandshake(final)");

		AuthenticationData authData = authDataMap.get(mi.getSourceGuid().getPrefix());
		X509Certificate cert = authData.getCertificate();

//		System.out.println("MI: " + getGuid().getPrefix() + ", " + mi.getSourceGuid().getPrefix() + 
//				", " + cert.getSerialNumber());
//		System.out.println(cert.getSerialNumber() + "::" + Arrays.toString(hFin.getEncryptedSharedSicret()));
		
		byte[] signedData = hFin.getSignedData();
		try {
			verify(signedData, cert.getPublicKey());
			byte[] encryptedSharedSecret = hFin.getEncryptedSharedSicret();			
			
			byte[] sharedSecret = decrypt(encryptedSharedSecret);
			// Register local key material also
			//getCryptoPlugin().setParticipantKeyMaterial(getLocalIdentity().getGuid().getPrefix(), sharedSecret);

			authData.setSharedSecret(sharedSecret);
			logger.info("{} Authenticated {} successfully", getGuid().getPrefix(), authData.getCertificate().getSubjectDN());
			notifyListenersOfSuccess(authData);
		} catch (InvalidKeyException | SignatureException | IllegalBlockSizeException | BadPaddingException e) {
			logger.warn("Failed to process handshake final message token", e);
			cancelHandshake(authData.getParticipantData());
		}
	}

	/**
	 * Signs given byte array with private key of local identity.
	 */
	private byte[] sign(byte[] bytesToSign) throws InvalidKeyException, SignatureException {
		byte[] signatureBytes = null;

		synchronized(signature) {
			signature.initSign(identity.getIdentityCredential().getPrivateKey());
			signature.update(bytesToSign);
			signatureBytes = signature.sign();
		}

		return signatureBytes;
	}

	/**
	 * Verifies that given byte array is signed by given public key.
	 */
	private boolean verify(byte[] signedBytes, PublicKey publicKey) throws InvalidKeyException, SignatureException {
		synchronized(signature) {
			signature.initVerify(publicKey);
			return signature.verify(signedBytes);
		}
	}

	/**
	 * Verifies that given certificate is signed by CA used.
	 */
	private void verify(X509Certificate certificate) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		logger.debug("Verifying {} is signed by {}", certificate.getSubjectDN(), caName);
		certificate.verify(ca.getPublicKey());
	}

	/**
	 * returns SHA-256 hash of the input bytes.  
	 */
	private byte[] hash(byte[] bytesToHash) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256"); // TODO: hardcoded
		return md.digest(bytesToHash);
	}

	/**
	 * encrypts given bytes with given public key.
	 */
	private byte[] encrypt(PublicKey publicKey, byte[] bytesToEncrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		synchronized (cipher) {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(bytesToEncrypt);
		}
	}

	/**
	 * decrypts given bytes with private key of local identity.
	 */
	private byte[] decrypt(byte[] bytesToDecrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		//System.out.println("Decrypting " + Arrays.toString(bytesToDecrypt));
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
		String s = "CHALLENGE:" + new BigInteger(96, random); // TODO: hardcoded
		return s.getBytes();
	}

	private byte[] createSharedSecret() {
		byte[] sharedSecret = new byte[20]; // TODO: hardcoded
		random.nextBytes(sharedSecret);

		return sharedSecret;
	}




	private LocalIdentity getLocalIdentity() {
		return identity;
	}

	private void cancelHandshake(ParticipantData participantData) {
		logger.debug("Canceling authentication handshake protocol for {}", participantData.getGuidPrefix());
		authDataMap.remove(participantData.getGuidPrefix());
		notifyListenersOfFailure(participantData);
	}


	@Override
	public Guid getGuid() {
		return identity.getGuid();
	}


	@Override
	public String getName() {
		return PLUGIN_NAME;
	}
}
