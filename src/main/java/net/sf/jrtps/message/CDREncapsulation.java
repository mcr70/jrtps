package net.sf.jrtps.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * CDREncapsulation is a general purpose binary DataEncapsulation. It holds a RTPSByteBuffer,
 * which can be used by marshallers.
 * 
 * @author mcr70
 * 
 */
public class CDREncapsulation extends DataEncapsulation {

    private final RTPSByteBuffer bb;
    @SuppressWarnings("unused")
    private short options;

    CDREncapsulation(RTPSByteBuffer bb) {
        this.bb = bb;
        this.options = (short) bb.read_short(); // NOT Used
    }

    public CDREncapsulation(int size) {
        this.bb = new RTPSByteBuffer(ByteBuffer.allocate(size));
        bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

        bb.write(CDR_LE_HEADER);
        // bb.write_short(options); // bb is positioned to start of actual data
    }

    @Override
    public boolean containsData() {
        return true;
    }

    @Override
    public byte[] getSerializedPayload() {
        byte[] serializedPayload = new byte[bb.position()];
        System.arraycopy(bb.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);

        return serializedPayload;
    }

    /**
     * Gets a RTPSByteBuffer, which can be used to marshall/unmarshall data.
     * 
     * @return RTPSByteBuffer
     */
    public RTPSByteBuffer getBuffer() {
        return bb;
    }
}
