package net.sf.jrtps.udds.security;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.message.Header;
import net.sf.jrtps.message.IllegalMessageException;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.SecureSubMessage;
import net.sf.jrtps.message.SubMessage;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CryptoPlugin.
 * 
 * @author mcr70
 */
public class CryptoPlugin {
	static final String CRYPTO_LOG_CATEGORY = "dds.sec.crypto";
	private static final Logger logger = LoggerFactory.getLogger(CRYPTO_LOG_CATEGORY);

	private static final Map<Integer,Transformer> transformersById = new ConcurrentHashMap<>();
	private static final Map<String,Transformer> transformersByName = new ConcurrentHashMap<>();

	private final Map<GuidPrefix, byte[]> participantKeyMaterials = new ConcurrentHashMap<>();

	private static int count = 0;
	
	static {
		NoOpTransformer noop = new NoOpTransformer();
		registerTransformer(noop);
		
		MACTransformer hmacSha1 = null;
		try {
			hmacSha1 = new MACTransformer(MACTransformer.HMAC_SHA1_NAME, MACTransformer.HMAC_SHA1);
			registerTransformer(hmacSha1);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", MACTransformer.HMAC_SHA1_NAME);
		}

		MACTransformer hmacSha256 = null;
		try {
			hmacSha256 = new MACTransformer(MACTransformer.HMAC_SHA256_NAME, MACTransformer.HMAC_SHA256);
			registerTransformer(hmacSha256);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", MACTransformer.HMAC_SHA1_NAME);
		}
		
		CipherTransformer aesCt = null; 
		try {
			aesCt = new CipherTransformer(CipherTransformer.AES_NAME, CipherTransformer.AES_KIND);
			registerTransformer(aesCt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Failed to register CipherTransformer with name {}", CipherTransformer.AES_NAME, e);
		}
		
		if (aesCt != null && hmacSha1 != null) {
			CompositeTransformer ct = 
					new CompositeTransformer(aesCt, hmacSha1, CompositeTransformer.AES_HMAC_SHA1);
			registerTransformer(ct);
		}

		if (aesCt != null && hmacSha256 != null) {
			CompositeTransformer ct = 
					new CompositeTransformer(aesCt, hmacSha256, CompositeTransformer.AES_HMAC_SHA256);
			registerTransformer(ct);
		}
	}
	
	private final Configuration conf;
	private final int transformationKind;
	
	CryptoPlugin(Configuration conf) {
		this.conf = conf;
		
		String rtpsProtection = conf.getRTPSProtection();
		Transformer transformerByName;
		try {
			transformerByName = getTransformerByName(rtpsProtection);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		transformationKind = transformerByName.getTransformationKind();
	}
	

	/**
	 * Registers a Transformer to this CryptoPlugin
	 * @param ct
	 */
	public static void registerTransformer(Transformer ct) {
		logger.debug("Registering Transformer {}({}) with kind {}", ct.getName(), 
				ct.getClass().getName(), ct.getTransformationKind());
		transformersById.put(ct.getTransformationKind(), ct);
		transformersByName.put(ct.getName(), ct);
	}
	
	/**
	 * This method is called by jRTPS when a message is being sent.
	 * @param message Message to be sent.
	 * @return An encoded message
	 * @throws SecurityException 
	 */
	public Message encodeMessage(Message message) throws SecurityException {
		if (transformationKind == 0) {
			return message;
		}
		
		Transformer ctr = getTransformer(transformationKind);
		logger.trace("encoding message with {}", ctr.getName());

		// Write message as is to buffer
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		// then encode it, and create new Message
		Key key = createKey(message.getHeader().getGuidPrefix());
		SecurePayload payload = ctr.encode(key, bb); 
		
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(false);
		
		Header hdr = new Header(message.getHeader().getGuidPrefix(), /* GuidPrefix.GUIDPREFIX_SECURED, */  
				message.getHeader().getVersion(), VendorId.VENDORID_SECURED);
		
		Message securedMessage = new Message(hdr);
		securedMessage.addSubMessage(ssm);
		
		return securedMessage;
	}

	/**
	 * Decodes a SecureSubMessage into Message. This method is called by message
	 * receiver to decode secured message.
	 *
	 * @param sourceGuidPrefix 
	 * @param msg SecureSubMessage to decode
	 * @return decoded Message
	 * @throws SecurityException 
	 */
	public Message decodeMessage(GuidPrefix sourceGuidPrefix, SecureSubMessage msg) throws SecurityException {
		Transformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());

		logger.trace("decoding message with {}", ctr.getName());

		Key key = createKey(sourceGuidPrefix);
		RTPSByteBuffer bb = ctr.decode(key, msg.getSecurePayload());

		Message message = null;
		try {
			message = new Message(bb);
		} catch (IllegalMessageException e) {
			logger.error("Failed to read message", e);
		}
		
		return message;
	}
	


	SecureSubMessage encodeSubMessage(int transformationKind, SubMessage message) throws SecurityException {
		Transformer ctr = getTransformer(transformationKind);
		
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		Key key = createKey(null);
		SecurePayload payload = ctr.encode(key, bb); // TODO: shared secret
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(true);
				
		return ssm;
	}

	SubMessage decodeSubMessage(SecureSubMessage msg) throws SecurityException {
		Transformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());
		Key key = createKey(null);
		RTPSByteBuffer bb = ctr.decode(key, msg.getSecurePayload());
		// TODO: Create SubMessage out of RTPSByteBuffer
		
		return null;
	}

	private Transformer getTransformer(int kind) throws SecurityException {
		Transformer transformer = transformersById.get(kind);
		if (transformer == null) {
			throw new SecurityException("Could not find Transformer with transformationKind " + 
					kind + ": " + transformersById.keySet());
		}
		
		return transformer;
	}

	private Transformer getTransformerByName(String trName) throws SecurityException {
		Transformer transformer = transformersByName.get(trName);
		if (transformer == null) {
			throw new SecurityException("Could not find Transformer with name " + trName + 
					": " + transformersByName.keySet());
		}
		
		return transformer;
	}

	/**
	 * Sets the key material associated with given Guid.
	 * @param guid Guid
	 * @param bytes key material
	 */
	void setParticipantKeyMaterial(GuidPrefix prefix, byte[] bytes) {
		participantKeyMaterials.put(prefix, bytes);
	}
	
	byte[] getParticipantKeyMaterial(GuidPrefix prefix) {
		return participantKeyMaterials.get(prefix);
	}
	
	private SecretKeySpec createKey(GuidPrefix prefix) throws SecurityException {
		byte[] keyMaterial = participantKeyMaterials.get(prefix);
		if (keyMaterial == null) {
			throw new SecurityException("No key material found for " + prefix);
		}
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Got exception", e);
		}
		byte[] digest = md.digest(keyMaterial);

		SecretKeySpec secretKeySpec = new SecretKeySpec(digest, "AES");
		return secretKeySpec;
	}	
}
