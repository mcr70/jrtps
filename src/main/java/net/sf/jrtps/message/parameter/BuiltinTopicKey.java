package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

/**
 * BuiltinTopicKey holds Guid of the entities.
 * 
 * <p>
 * BuiltinTopicKey mapping is not described in RTPS specification. Instead, its parameter id (0x005a)
 * mapping can be found from dds-xtypes-1.0 document (formal-12-11-10.pdf).
 * 
 * @author mcr70
 * 
 */
public class BuiltinTopicKey extends Parameter implements InlineQoS {

    BuiltinTopicKey() {
        super(ParameterEnum.PID_BUILTIN_TOPIC_KEY);
    }

    public BuiltinTopicKey(Guid guid) {
        super(ParameterEnum.PID_BUILTIN_TOPIC_KEY, guid.getBytes());
    }

    /**
     * Get the builtin topic key as byte array.
     * 
     * @return builtin topic key as byte array
     */
    public byte[] getAsBytes() {
        return getBytes();
    }
    
    /**
     * Gets the Guid.
     * @return Guid
     */
    public Guid getGuid() {
        return new Guid(getBytes());
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
        if (other instanceof BuiltinTopicKey) {
            return Arrays.equals(getBytes(), ((BuiltinTopicKey) other).getBytes());
        }
        
        return false;
    }
    
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }
}