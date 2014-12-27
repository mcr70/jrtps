package net.sf.jrtps.udds.security;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

class CryptoPlugin {
	static final String CRYPTO_LOG_CATEGORY = "dds.sec.crypto";
	private static final Logger logger = LoggerFactory.getLogger(CRYPTO_LOG_CATEGORY);

	private static final Map<Integer,CryptoTransformer> transformersById = new ConcurrentHashMap<>();
	private static final Map<String,CryptoTransformer> transformersByName = new ConcurrentHashMap<>();

	static {
		try {
			HMACTransformer hmac = new HMACTransformer(HMACTransformer.HMAC_SHA1_NAME, HMACTransformer.HMAC_SHA1);
			registerTransformer(hmac);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", HMACTransformer.HMAC_SHA1_NAME);
		}

		try {
			HMACTransformer hmac = new HMACTransformer(HMACTransformer.HMAC_SHA256_NAME, HMACTransformer.HMAC_SHA256);
			registerTransformer(hmac);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to register HMACTransformer with algorithm {}", HMACTransformer.HMAC_SHA1_NAME);
		}
	}
	
	private Configuration conf;
	
	CryptoPlugin(Configuration conf) {
		this.conf = conf;
	}
	
	/**
	 * Registers a CryptoTransformer with given name and kind
	 * @param ct
	 */
	public static void registerTransformer(CryptoTransformer ct) {
		logger.debug("Registering CryptoTransformer {}({}) with kind {}", ct.getName(), ct.getClass(), 
				ct.getTransformationKind());
		transformersById.put(ct.getTransformationKind(), ct);
		transformersByName.put(ct.getName(), ct);
	}
	
	public Message encodeMessage(int transformationKind, Message message) {
		CryptoTransformer ctr = getTransformer(transformationKind);

		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		SecurePayload payload = ctr.encode(bb); // TODO: shared secret
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(false);
		
		Header hdr = new Header(GuidPrefix.GUIDPREFIX_SECURED,  
				message.getHeader().getVersion(), VendorId.VENDORID_SECURED);
		
		Message securedMessage = new Message(hdr);
		securedMessage.addSubMessage(ssm);
		
		return securedMessage;
	}


	public SecureSubMessage encodeSubMessage(int transformationKind, SubMessage message) {
		CryptoTransformer ctr = getTransformer(transformationKind);
		
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[conf.getBufferSize()]);
		message.writeTo(bb);
		
		SecurePayload payload = ctr.encode(bb); // TODO: shared secret
		SecureSubMessage ssm = new SecureSubMessage(payload);
		ssm.singleSubMessageFlag(true);
				
		return ssm;
	}

	public Message decodeMessage(SecureSubMessage msg) {
		CryptoTransformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());
		
		RTPSByteBuffer bb = ctr.decode(msg.getSecurePayload());
		Message message = new Message(bb);
		
		return message;
	}
	
	public SubMessage decodeSubMessage(SecureSubMessage msg) {
		CryptoTransformer ctr = getTransformer(msg.getSecurePayload().getTransformationKind());
		
		RTPSByteBuffer bb = ctr.decode(msg.getSecurePayload());
		// TODO: Create SubMessage out of RTPSByteBuffer
		
		return null;
	}

	private CryptoTransformer getTransformer(int kind) {
		CryptoTransformer cryptoTransformer = transformersById.get(kind);
		if (cryptoTransformer == null) {
			throw new SecurityException("Could not find CryptoTransformer with transformationKind " + kind);
		}
		
		return cryptoTransformer;
	}
}
