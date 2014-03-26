package net.sf.jrtps.message;

import java.nio.ByteOrder;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This abstract class is a base class for different encapsulation schemes.
 * DataEncapsulation is used by <i>Data</i> submessage.
 * 
 * @author mcr70
 * @see Data
 */
public abstract class DataEncapsulation {
    public static final byte[] CDR_BE_HEADER = new byte[] { 0, 0, 0, 0 };
    public static final byte[] CDR_LE_HEADER = new byte[] { 0, 1, 0, 0 };
    public static final byte[] PL_CDR_BE_HEADER = new byte[] { 0, 2, 0, 0 };
    public static final byte[] PL_CDR_LE_HEADER = new byte[] { 0, 3, 0, 0 };

    /**
     * <i>This method is not implemented at the moment.</i> Currently, it is only possible to
     * use predefined DataEncapsulations: CDREncapsulation and ParameterListEncapsulation 
     * 
     * @param dEnc
     * @see CDREncapsulation
     * @see ParameterListEncapsulation
     */
    public static void registerDataEncapsulation(DataEncapsulation dEnc) {
        // TODO: implement me
    }

    /**
     * Checks whether this encapsulation holds data or key as serialized
     * payload.
     * 
     * @return true, if this encapsulation holds data
     */
    public abstract boolean containsData(); // as opposed to key

    /**
     * Gets the payload as a byte array. Payload must contain an encapsulation
     * identifier as first 2 bytes.
     * 
     * @return serialized payload
     */
    public abstract byte[] getSerializedPayload();

    /**
     * Creates an instance of DataEncapsulation. Encapsulation identified by
     * reading first 2 bytes of serializedPayload.
     * 
     * @param serializedPayload
     * @return DataEncapsulation
     */
    public static DataEncapsulation createInstance(byte[] serializedPayload) {
        RTPSByteBuffer bb = new RTPSByteBuffer(serializedPayload);
        byte[] encapsulationHeader = new byte[2];
        bb.read(encapsulationHeader);

        short eh = (short) (((short) encapsulationHeader[0] << 8) | (encapsulationHeader[1] & 0xff));

        switch (eh) {
        case 0:
        case 1:
            boolean littleEndian = (eh & 0x1) == 0x1;
            if (littleEndian) {
                bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
            } else {
                bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
            }

            return new CDREncapsulation(bb);
        case 2:
        case 3:

            littleEndian = (eh & 0x1) == 0x1;
            if (littleEndian) {
                bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
            } else {
                bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
            }

            return new ParameterListEncapsulation(bb);
        }

        // TODO: handle this more gracefully
        return null;
    }
}
