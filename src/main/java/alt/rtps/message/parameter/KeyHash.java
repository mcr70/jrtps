package alt.rtps.message.parameter;

import java.util.Arrays;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GUID_t;


/**
 * 
 * @author mcr70
 * @see 8.7.9 Key Hash, 9.6.3.3
 */
public class KeyHash extends Parameter {

	KeyHash() {
		super(ParameterEnum.PID_KEY_HASH);
	}
	
	// TODO: we could have constructor that takes a GUID. This could be used 
	//       with built-in data creation
	
	public KeyHash(byte[] bytes) {
		super(ParameterEnum.PID_KEY_HASH, bytes);
		
		if (bytes == null || bytes.length != 16) {
			throw new IllegalArgumentException("byte[] length must be 16");
		}
	}
	
	/**
	 * Get the key hash. Key hash is always of length 16;
	 * @return
	 */
	public byte[] getKeyHash() {
		return getBytes();
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}	

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
	}
}