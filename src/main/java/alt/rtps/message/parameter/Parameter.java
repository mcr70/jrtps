package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * Parameter is used to encapsulate data for builtin entities.
 * 
 * @author mcr70
 * @see 9.6.2.2.2 ParameterID values
 */
public abstract class Parameter {
	private ParameterEnum parameterId;
	private byte[] bytes;

	/**
	 * Constructs Parameter with null bytes. Bytes are expected to be read by
	 * read(RTPSByteBuffer, int) 
	 * 
	 * @param id
	 * @see read(RTPSByteBuffer, int)
	 */
	protected Parameter(ParameterEnum id) {
		this(id, null);
	}

	/**
	 * Constructs Parameter with given bytes.
	 * 
	 * @param id
	 * @param bytes
	 */
	protected Parameter(ParameterEnum id, byte[] bytes) {
		this.parameterId = id;
		this.bytes = bytes;
	}

	/**
	 * Get the parameterId of this parameter.
	 * @return
	 * @see 9.6.2.2.2 ParameterID values
	 */
	public ParameterEnum getParameterId() {
		return parameterId;
	}

	/**
	 * Parameter value
	 * @return
	 */
	public byte[] getBytes() {
		return bytes;
	}
	
	public abstract void read(RTPSByteBuffer bb, int length);

	/**
	 * This method can be used by implementing classes to read bytes of this parameter to byte array.
	 * 
	 * @param bb
	 * @param length
	 */
	protected final void readBytes(RTPSByteBuffer bb, int length) {
		this.bytes = new byte[length];
		bb.read(bytes);		
	}

	/**
	 * This method can be used by implementing classes to write bytes of this parameter to RTPSByteBuffer.
	 * This method must be paired with readBytes(RTPSByteBuffer, int).
	 * 
	 * @param bb
	 */
	protected final void writeBytes(RTPSByteBuffer bb) {
		bb.write(getBytes());
	}
	
	public abstract void writeTo(RTPSByteBuffer bb);
	
	public String toString() {
		if (bytes != null) {
			return getClass().getSimpleName() + Arrays.toString(bytes);
		}
		
		return getClass().getSimpleName();
	}
}
