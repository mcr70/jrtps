package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * 
 * @author mcr70
 *
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

	//public abstract void read(RTPSByteBuffer bb, int length);
	public void read(RTPSByteBuffer bb, int length) {
		this.value = new byte[length];
		bb.read(value);
	}

	public String toString() {
		return getClass().getSimpleName();
	}
	

	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_short(getParameterId().kind());
		byte[] bytes = getBytes(); //  TODO: make abstract
		buffer.write_short((short) bytes.length);
		buffer.write(bytes);
	}

	

}
