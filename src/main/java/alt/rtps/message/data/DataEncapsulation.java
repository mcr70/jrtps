package alt.rtps.message.data;

import java.nio.ByteOrder;

import alt.rtps.transport.RTPSByteBuffer;

/**
 * This abstract class is a base class for different encapsulation schemes.
 * 
 * @author mcr70
 *
 */
public abstract class DataEncapsulation {
	public static final byte[] CDR_BE_HEADER = new byte[] {0,0, 0,0};
	public static final byte[] CDR_LE_HEADER = new byte[] {0,1, 0,0};
	public static final byte[] PL_CDR_BE_HEADER = new byte[] {0,2, 0,0};
	public static final byte[] PL_CDR_LE_HEADER = new byte[] {0,3, 0,0};
	
	
	public static void registerDataEncapsulation(DataEncapsulation dEnc) {
		// TODO: implement me
	}
	
	/**
	 * Checks whether this encapsulation holds data or key as serialized payload.
	 * @return true, if this encapsulation holds data
	 */
	public abstract boolean containsData(); // as opposed to key 
	
	/**
	 * Gets the payload as a byte array. Payload must contain an encapsulation identifier
	 * as first 2 bytes.
	 * 
	 * @return
	 */
	public abstract byte[] getSerializedPayload();


	/**
	 * Creates an instance of DataEncapsulation. Encapsulation identified by reading 
	 * first 2 bytes of serializedPayload.
	 * 
	 * @param serializedPayload
	 * @return
	 */
	public static DataEncapsulation createInstance(byte[] serializedPayload) {
		RTPSByteBuffer bb = new RTPSByteBuffer(serializedPayload);
		byte[] encapsulationHeader = new byte[2];
		bb.read(encapsulationHeader);
		
		short eh = (short) (((short)encapsulationHeader[0] << 8) | encapsulationHeader[1]);
		
		switch (eh) {
		case 0:
		case 1:
			boolean littleEndian = (eh & 0x1) == 0x1;
			if (littleEndian) {
				bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
			}
			else {
				bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
			}

			return new CDREncapsulation(bb);
		case 2:
		case 3:
			
			littleEndian = (eh & 0x1) == 0x1;
			if (littleEndian) {
				bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
			}
			else {
				bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
			}
			
			return new ParameterListEncapsulation(bb);
		}
		
		
		// TODO: handle this more gracefully
		return null;
	}
}
