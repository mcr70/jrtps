package net.sf.jrtps.udds.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CipherTransformer implements Transformer {
	private static final Logger logger = LoggerFactory.getLogger(CryptoPlugin.CRYPTO_LOG_CATEGORY);

	public static final String AES128_HMAC_SHA1_NAME = "aes128_hmac_sha1";
    public static final String AES256_HMAC_SHA256_NAME = "aes256_hmac_sha256";
    public static final int AES128_HMAC_SHA1 = 0x00000200;
    public static final int AES256_HMAC_SHA256 = 0x00000201;
	
    private String name;
	private int kind;
	private Cipher instance;
    
    public CipherTransformer(String cipherName, int kind) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.name = cipherName;
		this.kind = kind;
		instance = Cipher.getInstance(cipherName);
    }
    
	@Override
	public int getTransformationKind() {
		return kind;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SecurePayload encode(Key key, RTPSByteBuffer bb) {
		try {
			synchronized (instance) {
				instance.init(Cipher.ENCRYPT_MODE, key);
				byte[] bytes = null;
				instance.doFinal(bb.getBuffer(), null); // TODO
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
			logger.warn("Failed to encode message", e);
		}
		
		return null;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) {
		Key certificate = null;
		
		try {
			synchronized (instance) {
				instance.init(Cipher.DECRYPT_MODE, certificate);
				byte[] bytes = null;
				byte[] decryptedBytes = instance.doFinal(bytes);
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			logger.warn("Failed to decode message", e);
		}
		
		return null;
	}
}
