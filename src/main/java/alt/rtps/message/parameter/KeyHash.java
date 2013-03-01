package alt.rtps.message.parameter;

import java.util.Arrays;


/**
 * 
 * @author mcr70
 * @see 8.7.9 Key Hash, 9.6.3.3
 */
public class KeyHash extends Parameter {
	KeyHash() {
		super(ParameterEnum.PID_KEY_HASH);
	}
	
	/**
	 * Get the key hash. Key hash is always of length 16;
	 * @return
	 */
	public byte[] getKeyHash() {
		return getBytes();
	}

	public String toString() {
		return super.toString() + Arrays.toString(getKeyHash());
	}	
}