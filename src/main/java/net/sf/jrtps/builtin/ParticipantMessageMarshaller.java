package net.sf.jrtps.builtin;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

/**
 * Marshaller for builtin data for topic <i>DCPSParticipantMessage</i>. With
 * jRTPS, instances of this topic is of type ParticipantMessage.
 * 
 * @author mcr70
 */
public class ParticipantMessageMarshaller implements Marshaller<ParticipantMessage> {

    private int bufferSize = 256;

    /**
     * ParticipantMessage data has always a key.
     * 
     * @return true
     */
    @Override
    public boolean hasKey() {
        return true; // hardcoded. key is guid
    }

    /**
     * Extracts a key from given ParticipantMessage.
     */
    @Override
    public byte[] extractKey(ParticipantMessage data) {
        byte[] prefix = data.getGuidPrefix().getBytes();
        byte[] kind = data.getKind();

        byte[] key = new byte[16];
        System.arraycopy(prefix, 0, key, 0, 12);
        System.arraycopy(kind, 0, key, 12, 4);

        return key;
    }

    @Override
    public ParticipantMessage unmarshall(DataEncapsulation dEnc) throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        GuidPrefix prefix = new GuidPrefix(bb);
        byte[] kind = new byte[4];
        bb.read(kind);

        int dataLength = bb.read_long();
        byte[] data = new byte[dataLength];
        bb.read(data);

        return new ParticipantMessage(prefix, kind, data);
    }

    @Override
    public DataEncapsulation marshall(ParticipantMessage data) throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        data.getGuidPrefix().writeTo(bb);
        bb.write(data.getKind());

        byte[] dataBytes = data.getData();

        bb.write_long(dataBytes.length);
        bb.write(dataBytes);

        return cdrEnc;
    }
}
