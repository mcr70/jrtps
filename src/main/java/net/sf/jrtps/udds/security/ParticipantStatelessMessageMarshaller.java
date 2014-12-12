package net.sf.jrtps.udds.security;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * ParticipantStatelessMessageMarshaller is used during authentication to marshall/unmarshall
 * <i>ParticipantStatelessMessage</i>s
 * 
 * @author mcr70
 */
public class ParticipantStatelessMessageMarshaller implements Marshaller<ParticipantStatelessMessage> {

	@Override
	public boolean hasKey() {
		return false;
	}

	@Override
	public byte[] extractKey(ParticipantStatelessMessage data) {
		return null;
	}

	@Override
	public ParticipantStatelessMessage unmarshall(DataEncapsulation dEnc) throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();
        
        ParticipantStatelessMessage psm = null;
		try {
			psm = new ParticipantStatelessMessage(bb);
		} catch (Exception e) {
			throw new IOException(e);
		}
        
		return psm;
	}

	@Override
	public DataEncapsulation marshall(ParticipantStatelessMessage data)
			throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(2048); // TODO: hardcoded
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        data.writeTo(bb);
        
        return cdrEnc;
	}
}
