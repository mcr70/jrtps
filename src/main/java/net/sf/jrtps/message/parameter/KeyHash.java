package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 8.7.9 Key Hash, 9.6.3.3
 * 
 * @author mcr70
 * 
 */
public class KeyHash extends Parameter implements InlineParameter {
    KeyHash() {
        super(ParameterEnum.PID_KEY_HASH);
    }

    public KeyHash(byte[] bytes) {
        super(ParameterEnum.PID_KEY_HASH, bytes);

        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("byte[] length must be 16");
        }
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
}