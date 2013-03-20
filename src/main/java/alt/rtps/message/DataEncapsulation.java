package alt.rtps.message;

import java.nio.ByteOrder;

import alt.rtps.message.data.CDREncapsulation;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.transport.RTPSByteBuffer;

public abstract class DataEncapsulation {
	public static final byte[] CDR_BE_HEADER = new byte[] {0,0, 0,0};
	public static final byte[] CDR_LE_HEADER = new byte[] {0,1, 0,0};
	public static final byte[] PL_CDR_BE_HEADER = new byte[] {0,2, 0,0};
	public static final byte[] PL_CDR_LE_HEADER = new byte[] {0,3, 0,0};
	
	
	public static void registerDataEncapsulation(DataEncapsulation dEnc) {
		// TODO: implement me
	}
	

	//public abstract byte[] getEncapsulationHeader();
	public abstract boolean containsData(); // as opposed to key 
	
	/**
	 * Gets the payload as a byte array. Payload must contain an encapsulation identifier
	 * as first 2 bytes.
	 * 
	 * @return
	 */
	public abstract byte[] getSerializedPayload();


	public static DataEncapsulation createInstance(byte[] serializedPayload) {
		RTPSByteBuffer bb = new RTPSByteBuffer(serializedPayload);
		byte[] encapsulationHeader = new byte[2];
		bb.read(encapsulationHeader);
		
		short eh = (short) (((short)encapsulationHeader[0] << 8) | encapsulationHeader[1]);
		short options = 0;
		switch (eh) {
		case 0:
		case 1:
			options = (short) bb.read_short();
			boolean littleEndian = (eh & 0x1) == 0x1;
			if (littleEndian) {
				bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
			}
			else {
				bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
			}

			return new CDREncapsulation(bb, options);
		case 2:
		case 3:
			options = (short) bb.read_short();
			littleEndian = (eh & 0x1) == 0x1;
			if (littleEndian) {
				bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
			}
			else {
				bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
			}
			
			ParameterList pl = new ParameterList(bb);
			return new ParameterListEncapsulation(pl, littleEndian);
		}
		
		
		// TODO: handle this more gracefully
		return null;
	}
}
