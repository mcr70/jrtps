package alt.rtps.message;

public abstract class DataEncapsulation {
	public static final byte[] CDR_BE_HEADER = new byte[] {0,0, 0,0};
	public static final byte[] CDR_LE_HEADER = new byte[] {0,1, 0,0};
	public static final byte[] PL_CDR_BE_HEADER = new byte[] {0,2, 0,0};
	public static final byte[] PL_CDR_LE_HEADER = new byte[] {0,3, 0,0};
	
	//public abstract byte[] getEncapsulationHeader();
	public abstract boolean containsData(); // as opposed to key 
	/**
	 * Gets the payload as a byte array. Payload must contain an encapsulation header
	 * as first 4 bytes.
	 * 
	 * @return
	 */
	public abstract byte[] getSerializedPayload();
}
