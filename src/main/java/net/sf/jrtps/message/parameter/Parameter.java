package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Parameter is used to encapsulate data for builtin entities. see 9.6.2.2.2
 * ParameterID values
 * 
 * @author mcr70
 * 
 */
public abstract class Parameter {
    private ParameterId parameterId;
    private byte[] bytes;

    /**
     * Constructs Parameter with null bytes. Bytes are expected to be read by
     * read(RTPSByteBuffer, int)
     * 
     * @param id ParameterId
     * @see #read(RTPSByteBuffer, int)
     */
    protected Parameter(ParameterId id) {
        this(id, null);
    }

    /**
     * Constructs Parameter with given bytes.
     * 
     * @param id ParameterId
     * @param bytes byte array of Parameter
     */
    protected Parameter(ParameterId id, byte[] bytes) {
        this.parameterId = id;
        this.bytes = bytes;
    }

    /**
     * Get the parameterId of this parameter. see 9.6.2.2.2 ParameterID values
     * 
     * @return ParameterEnum
     * 
     */
    public ParameterId getParameterId() {
        return parameterId;
    }

    /**
     * Parameter value
     * 
     * @return bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    public abstract void read(RTPSByteBuffer bb, int length);

    /**
     * This method can be used by implementing classes to read bytes of this
     * parameter to byte array.
     * 
     * @param bb RTPSByteBuffer, that is used
     * @param length number of bytes
     */
    protected final void readBytes(RTPSByteBuffer bb, int length) {
        this.bytes = new byte[length];
        bb.read(bytes);
    }

    /**
     * This method can be used by implementing classes to write bytes of this
     * parameter to RTPSByteBuffer. This method must be paired with
     * readBytes(RTPSByteBuffer, int).
     * 
     * @param bb Writes bytes into this RTPSByteBuffer
     */
    protected final void writeBytes(RTPSByteBuffer bb) {
        bb.write(getBytes());
    }

    /**
     * Writes this Parameter into given RTPSByteBuffer
     * @param bb RTPSByteBuffer
     */
    public abstract void writeTo(RTPSByteBuffer bb);

    @Override
    public String toString() {
        if (bytes != null) {
            return getClass().getSimpleName() + Arrays.toString(bytes);
        }

        return getClass().getSimpleName();
    }
}
