package net.sf.jrtps.message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.message.parameter.ParameterFactory;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumber;

/**
 * see 8.3.7.3 DataFrag
 * 
 * @author mcr70
 * 
 */
public class DataFrag extends SubMessage {
    public static final int KIND = 0x16;

    private short extraFlags;
    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumber writerSN;
    private int fragmentStartingNum;
    private int fragmentsInSubmessage;
    private int fragmentSize;
    private int sampleSize;

    private List<Parameter> parameterList = new LinkedList<Parameter>();
    private byte[] serializedPayload;

    public DataFrag(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    public boolean inlineQosFlag() {
        return (header.flags & 0x2) != 0;
    }

    public boolean keyFlag() {
        return (header.flags & 0x4) != 0;
    }

    public EntityId getReaderId() {
        return readerId;
    }

    public EntityId getWriterId() {
        return writerId;
    }

    public SequenceNumber getWriterSequenceNumber() {
        return writerSN;
    }

    public int getFragmentStartingNumber() {
        return fragmentStartingNum;
    }

    public int getFragmentsInSubmessage() {
        return fragmentsInSubmessage;
    }

    public int getFragmentSize() {
        return fragmentSize;
    }

    public int getSampleSize() { // getDataSize()
        return sampleSize;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public byte[] getSerializedPayload() {
        return serializedPayload;
    }

    private void readMessage(RTPSByteBuffer bb) {
        int start_count = bb.position(); // start of bytes read so far from the
                                         // beginning

        this.extraFlags = (short) bb.read_short();
        int octetsToInlineQos = bb.read_short() & 0xffff;

        int currentCount = bb.position(); // count bytes to inline qos

        this.readerId = EntityId.readEntityId(bb);
        this.writerId = EntityId.readEntityId(bb);
        this.writerSN = new SequenceNumber(bb);

        this.fragmentStartingNum = bb.read_long(); // ulong
        this.fragmentsInSubmessage = bb.read_short(); // ushort
        this.fragmentSize = bb.read_short(); // ushort
        this.sampleSize = bb.read_long(); // ulong

        int bytesRead = bb.position() - currentCount;
        int unknownOctets = octetsToInlineQos - bytesRead;

        for (int i = 0; i < unknownOctets; i++) {
            bb.read_octet(); // Skip unknown octets, @see 9.4.5.3.3 octetsToInlineQos
        }

        if (inlineQosFlag()) {
            readParameterList(bb);
        }

        int end_count = bb.position(); // end of bytes read so far from the beginning

        this.serializedPayload = new byte[header.submessageLength - (end_count - start_count)];
        bb.read(serializedPayload);
    }

    /**
     * 
     * @param bb
     * @throws IOException
     * @see 9.4.2.11 ParameterList
     */
    private void readParameterList(RTPSByteBuffer bb) {
        while (true) {
            bb.align(4);
            Parameter param = ParameterFactory.readParameter(bb);
            parameterList.add(param);
            if (param.getParameterId() == ParameterId.PID_SENTINEL) {
                break; 
            }
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_short(extraFlags);

        short octets_to_inline_qos = 4 + 4 + 8 + 4 + 2 + 2 + 4;
        bb.write_short(octets_to_inline_qos);

        readerId.writeTo(bb);
        writerId.writeTo(bb);
        writerSN.writeTo(bb);

        bb.write_long(fragmentStartingNum);
        bb.write_short((short) fragmentsInSubmessage);
        bb.write_short((short) fragmentSize);
        bb.write_long(sampleSize);

        if (inlineQosFlag()) {
            writeParameterList(bb);
        }

        bb.write(serializedPayload); // TODO: check this
    }

    private void writeParameterList(RTPSByteBuffer buffer) {
        for (Parameter param : parameterList) {
            param.writeTo(buffer);
        }

        // TODO: last Parameter must be PID_SENTINEL
    }
}
