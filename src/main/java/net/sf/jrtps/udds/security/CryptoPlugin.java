package net.sf.jrtps.udds.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.message.Header;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.SecureSubMessage;
import net.sf.jrtps.message.SubMessage;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoPlugin {
	static final String CRYPTO_LOG_CATEGORY = "dds.sec.crypto";
	private static final Logger logger = LoggerFactory.getLogger(CRYPTO_LOG_CATEGORY);

	private static final Map<Integer,Transformer> transformersById = new ConcurrentHashMap<>();
	private static final Map<String,Transformer> transformersByName = new ConcurrentHashMap<>();

	static {
		NoOpTransformer noop = new NoOpTransformer();
		registerTransformer(noop);
		
		try {
			MACTransformer hmac = new MACTransformer(MACTransformer.HMAC_SHA1_NAME, MACTransformer.HMAC_SHA1);
			registerTransformer(hmac);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", MACTransformer.HMAC_SHA1_NAME);
		}

		try {
			MACTransformer hmac = new MACTransformer(MACTransformer.HMAC_SHA256_NAME, MACTransformer.HMAC_SHA256);
			registerTransformer(hmac);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", MACTransformer.HMAC_SHA1_NAME);
		}
	}
	
	private final Configuration conf;
	private final int transformationKind;
	
	public CryptoPlugin(Configuration conf) {
		this.conf = conf;
		String rtpsProtection = conf.getRTPSProtection();
		Transformer transformerByName = getTransformerByName(rtpsProtection);
		transformationKind = transformerByName.getTransformationKind();
	}
	

	/**
	 * Registers a CryptoTransformer with given name and kind
	 * @param ct
	 */
	public static void registerTransformer(Transformer ct) {
		logger.debug("Registering Transformer {}({}) with kind {}", ct.getName(), 
				ct.getClass().getName(), ct.getTransformationKind());
		transformersById.put(ct.getTransformationKind(), ct);
		transformersByName.put(ct.getName(), ct);
	}
	
	public Message encodeMessage(Message message) {
		if (transformationKind == 0) {
			return message;
		}
		
		Transformer ctr = getTransformer(transformationKind);
		logger.trace("encoding message with {}", ctr.getName());

		// Write message as is to buffer
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		// then encode it, and create new Message
		Key key = createKey(message);
		SecurePayload payload = ctr.encode(key, bb); // TODO: shared secret
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(false);
		
		Header hdr = new Header(GuidPrefix.GUIDPREFIX_SECURED,  
				message.getHeader().getVersion(), VendorId.VENDORID_SECURED);
		
		Message securedMessage = new Message(hdr);
		securedMessage.addSubMessage(ssm);
		
		return securedMessage;
	}


	public SecureSubMessage encodeSubMessage(int transformationKind, SubMessage message) {
		Transformer ctr = getTransformer(transformationKind);
		
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		Key key = createKey(null);
		SecurePayload payload = ctr.encode(key, bb); // TODO: shared secret
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(true);
				
		return ssm;
	}

	public Message decodeMessage(SecureSubMessage msg) {
		Transformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());

		logger.trace("decoding message with {}", ctr.getName());

		Key key = createKey(null);
		RTPSByteBuffer bb = ctr.decode(key, msg.getSecurePayload());
		Message message = new Message(bb);
		
		return message;
	}
	
	public SubMessage decodeSubMessage(SecureSubMessage msg) {
		Transformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());
		Key key = createKey(null);
		RTPSByteBuffer bb = ctr.decode(key, msg.getSecurePayload());
		// TODO: Create SubMessage out of RTPSByteBuffer
		
		return null;
	}

	private Transformer getTransformer(int kind) {
		Transformer cryptoTransformer = transformersById.get(kind);
		if (cryptoTransformer == null) {
			throw new SecurityException("Could not find CryptoTransformer with transformationKind " + 
					kind + ": " + transformersById.keySet());
		}
		
		return cryptoTransformer;
	}

	private Transformer getTransformerByName(String trName) {
		Transformer cryptoTransformer = transformersByName.get(trName);
		if (cryptoTransformer == null) {
			throw new SecurityException("Could not find CryptoTransformer with name " + trName + 
					": " + transformersByName.keySet());
		}
		
		return cryptoTransformer;
	}

	private SecretKeySpec createKey(Message msg) {
		
		//	    MessageDigest md = MessageDigest.getInstance(hashName);
//	    byte[] digest = md.digest(convertme);

		// TODO: key generation
		SecretKeySpec secretKeySpec = new SecretKeySpec("sharedsecret".getBytes(), "AES");
		return secretKeySpec;
	}	
}
