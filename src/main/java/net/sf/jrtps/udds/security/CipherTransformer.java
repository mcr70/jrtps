package net.sf.jrtps.udds.security;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CipherTransformer uses javax.crypto.Cipher to encode and decode payload. 
 * 
 * @see javax.crypto.Cipher
 * @author mcr70
 */
public class CipherTransformer implements Transformer {
	private static final Logger logger = LoggerFactory.getLogger(CryptoPlugin.CRYPTO_LOG_CATEGORY);

	public static final int AES_KIND = 0xff000200;
	public static final String AES_NAME = "AES";

	public static final String AES128_HMAC_SHA1_NAME = "aes128_hmac_sha1";
    public static final String AES256_HMAC_SHA256_NAME = "aes256_hmac_sha256";
    public static final int AES128_HMAC_SHA1 = 0x00000200;
    public static final int AES256_HMAC_SHA256 = 0x00000201;
	
    private final String name;
	private final int kind;
	private final Cipher cipher;
    
	/**
	 * Constructor for CipherTransformer. An instance of Cipher is 
	 * obtained by calling Cipher.getInstance(cipherName).
	 * 
	 * @param cipherName Name of the transformation
	 * @param kind transformationKind
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
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
	public SecurePayload encode(Key key, RTPSByteBuffer bb) throws SecurityException {
		ByteBuffer output = ByteBuffer.wrap(new byte[4096]);// TODO: hardcoded
		bb.getBuffer().flip();

		synchronized (cipher) {
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key);
				cipher.doFinal(bb.getBuffer(), output);
			} catch (InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
				throw new SecurityException(e);
			}
		}
		
		// TODO: securepayload w/ ByteBuffer
		byte[] array = output.array();

		byte[] cipherText = new byte[output.position()];
		System.arraycopy(array, 0, cipherText, 0, cipherText.length);

		SecurePayload payload = new SecurePayload(kind, cipherText);
		if (logger.isTraceEnabled()) {
			logger.trace("CipherTransformer: encoded payload: {}", Arrays.toString(payload.getCipherText()));
		}
		
		return payload;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) throws SecurityException {
		byte[] decryptedBytes = null;
		try {
			synchronized (cipher) {
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] cipherText = payload.getCipherText();
				decryptedBytes = cipher.doFinal(cipherText);
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new SecurityException(e);
		}
		
		return new RTPSByteBuffer(decryptedBytes);
	}
}
