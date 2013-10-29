package net.sf.jrtps.types;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * BuiltinTopicKey is mapped to GuidPrefix, and it is obtained from PID_PARTICIPANT_GUID
 * during SEPD & SPDP.
 * see table 9.13: PID_PARTICIPANT_GUID & table 9.10
 * 
 * @author mcr70
 * 
 */
public class BuiltinTopicKey_t {
	public int key[] = null; // int[3]
	
	public BuiltinTopicKey_t(int[] key) {
		this.key = key;
	}

	public int[] getKey() {
		return key;
	}
		
	public void writeTo(RTPSByteBuffer buffer) {		
		buffer.write(key);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(key);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BuiltinTopicKey_t) {
			BuiltinTopicKey_t other = (BuiltinTopicKey_t) o;
			
			return Arrays.equals(key, other.key);
		}
		
		return false;
	}

	public String toString() {
		return "BuiltinTopicKey_t: " + Arrays.toString(key);
	}
}
