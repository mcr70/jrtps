import java.io.IOException;

import alt.rtps.RTPSParticipant;
import alt.rtps.message.DataEncapsulation;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.EntityId_t;


public class ParticipantClient {
	public static void main(String[] args) throws IOException {		
		RTPSParticipant p = new RTPSParticipant(0); // Participant to domain 0

		createHelloReader(p);
		
		p.start(); // start Participant. Threads will start and initial messages are sent
	}

	private static void createHelloReader(RTPSParticipant p) {
		p.createReader(new EntityId_t.UserDefinedEntityId(new byte[]{0,0,1}, (byte)0x07), 
				"HelloTopic", new Marshaller() {
					@Override
					public Object unmarshall(DataEncapsulation dEnc) {
						System.out.println("Unmarshall !!!!!!!!!!!!!!!!!!!!!");
						return null;
					}

					@Override
					public DataEncapsulation marshall(Object data) {
						System.out.println("Marhsall !!!!!!!!!!!!!!!!!!!!!!!");
						return null;
					}
				});
	}
}
