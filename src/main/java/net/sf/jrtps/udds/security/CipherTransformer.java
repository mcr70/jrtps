package net.sf.jrtps.udds.security;

import java.nio.ByteBuffer;
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

	public static final int AES128 = 0xff000200;
	public static final String AES128_NAME = "AES";

	public static final String AES128_HMAC_SHA1_NAME = "aes128_hmac_sha1";
    public static final String AES256_HMAC_SHA256_NAME = "aes256_hmac_sha256";
    public static final int AES128_HMAC_SHA1 = 0x00000200;
    public static final int AES256_HMAC_SHA256 = 0x00000201;
	
    private String name;
	private int kind;
	private Cipher cipher;
    
    public CipherTransformer(String cipherName, int kind) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.name = cipherName;
		this.kind = kind;
		cipher = Cipher.getInstance(cipherName);
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
		ByteBuffer output = ByteBuffer.wrap(new byte[4096]);// TODO: hardcoded
		bb.getBuffer().flip();
		try {
			synchronized (cipher) {
				cipher.init(Cipher.ENCRYPT_MODE, key);
				cipher.doFinal(bb.getBuffer(), output); // TODO
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
			logger.warn("Failed to encode message", e);
		}
		
		// TODO: securepayload w/ ByteBuffer
		byte[] array = output.array();
		byte[] cipherText = new byte[output.position()];
		System.arraycopy(array, 0, cipherText, 0, cipherText.length);
		
		SecurePayload payload = new SecurePayload(kind, cipherText);
		return payload;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) {
		byte[] decryptedBytes = null;
		try {
			synchronized (cipher) {
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] cipherText = payload.getCipherText();
				decryptedBytes = cipher.doFinal(cipherText);
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			logger.warn("Failed to decode message", e);
		}
		
		return new RTPSByteBuffer(decryptedBytes);
	}
}
