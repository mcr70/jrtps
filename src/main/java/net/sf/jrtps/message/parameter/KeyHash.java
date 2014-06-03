package net.sf.jrtps.message.parameter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 8.7.9 Key Hash, 9.6.3.3
 * 
 * @author mcr70
 * 
 */
public class KeyHash extends Parameter implements InlineParameter {

    private static MessageDigest md5 = null;
    private static NoSuchAlgorithmException noSuchAlgorithm = null;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlgorithm = e; // Actual usage might not even need it.
        }
    }    


    KeyHash() {
        super(ParameterEnum.PID_KEY_HASH);
    }

    public KeyHash(byte[] bytes) {
        this(bytes, false);
    }

    public KeyHash(byte[] bytes, boolean isBuiltinKey) {
        super(ParameterEnum.PID_KEY_HASH, prepareKey(bytes, isBuiltinKey));
    }

    /**
     * Get the key hash. Key hash is always of length 16;
     * 
     * @return Key hash as byte array
     */
    public byte[] getKeyHash() {
        return getBytes();
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }
    
    public boolean equals(Object other) {
        if (other instanceof KeyHash) {
            return Arrays.equals(getKeyHash(), ((KeyHash)other).getKeyHash());
        }
        
        return false;
    }
    
    public int hashCode() {
        return Arrays.hashCode(getKeyHash());
    }

    private static byte[] prepareKey(byte[] key, boolean isBuiltinKey) {
        if (isBuiltinKey) {
            return key; // key is BuiltinTopicKey. Leave as is, no MD5 hashing
        }
        
        if (key == null) {
            key = new byte[0];
        }
        
        byte[] bytes = null;
        if (key.length < 16) {
            bytes = new byte[16];
            System.arraycopy(key, 0, bytes, 0, key.length);
        } else {
            if (md5 == null) {
                throw new RuntimeException(noSuchAlgorithm);
            }

            synchronized (md5) {
                bytes = md5.digest(key);
                md5.reset();                
            }
        }

        return bytes;
    }
}