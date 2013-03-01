package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * Parameter is used to encapsulate data for builtin entities.
 * 
 * @author mcr70
 * @see 9.6.2.2.2 ParameterID values
 */
public abstract class Parameter {
	private ParameterEnum parameterId;
	private byte[] value;

	protected Parameter(ParameterEnum id) {
		this.parameterId = id;
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
		return value;
	}

	public abstract void read(RTPSByteBuffer bb, int length);
	//	public void read(RTPSByteBuffer bb, int length) {
	//		this.value = new byte[length];
	//		bb.read(value);
	//	}

	/**
	 * This method can be used by implementing classes to read bytes of this parameter to byte array.
	 * 
	 * @param bb
	 * @param length
	 */
	protected final void readBytes(RTPSByteBuffer bb, int length) {
		this.value = new byte[length];
		bb.read(value);		
	}

	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_short(getParameterId().kind());
		byte[] bytes = getBytes(); //  TODO: make abstract
		buffer.write_short((short) bytes.length);
		buffer.write(bytes);

		// NOT called at the moment. paramterid.kind & length is calculated outside of this method
		// @see Data.writeParameterList()
		throw new RuntimeException("******************");

	}

	public String toString() {
		return getClass().getSimpleName();
	}
}
