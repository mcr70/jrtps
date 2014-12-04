package net.sf.jrtps.udds.security;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

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
	public ParticipantStatelessMessage unmarshall(DataEncapsulation dEnc)
			throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();
        
		return new ParticipantStatelessMessage(bb);
	}

	@Override
	public DataEncapsulation marshall(ParticipantStatelessMessage data)
			throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(1024);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        // TODO: Implement me
        
        return null;
	}
}
