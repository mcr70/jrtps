package net.sf.jrtps.udds.security;

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
	public static final String CRYPTO_LOG_CATEGORY = "dds.sec.crypto";

    // See 9.5.2.2 DDS:Crypto:AES-CTR-HMAC-RSA/DSA-DH CryptoTransformIdentifier
    // for predefined transformation_kind_id values: 
    public static final int HMAC_SHA1 = 0x00000100;
    public static final int HMAC_SHA256 = 0x00000101;
    public static final int AES128_HMAC_SHA1 = 0x00000200;
    public static final int AES256_HMAC_SHA256 = 0x00000201;
    
	private static final Logger logger = LoggerFactory.getLogger(CRYPTO_LOG_CATEGORY);
	private static final Map<Integer,CryptoTransformer> transformers = new ConcurrentHashMap<>();

	private Configuration conf;
	
	CryptoPlugin(Configuration conf) {
		this.conf = conf;
	}
	
	public static void registerTransformer(int transformationKind, CryptoTransformer t) {
		logger.debug("Registering CryptoTransformer {} with kind {}", t.getClass(), transformationKind);
		transformers.put(transformationKind, t);
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
		CryptoTransformer cryptoTransformer = transformers.get(kind);
		if (cryptoTransformer == null) {
			throw new SecurityException("Could not find CryptoTransformer with transformationKind " + kind);
		}
		
		return cryptoTransformer;
	}
}
