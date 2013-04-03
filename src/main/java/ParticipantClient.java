import java.io.IOException;

import alt.rtps.RTPSParticipant;
import alt.rtps.RTPSReader;
import alt.rtps.RTPSWriter;
import alt.rtps.message.DataEncapsulation;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.EntityId_t;


public class ParticipantClient {
	public static void main(String[] args) throws IOException {		
		RTPSParticipant p = new RTPSParticipant(0); // Participant to domain 0
		
		//createHelloReader(p);
		RTPSWriter w = createHelloWriter(p);
		w.createChange(new HelloWorldData(1, "mika"));

		p.start(); // start Participant. Threads will start and initial messages are sent
	}

	private static RTPSReader createHelloReader(RTPSParticipant p) {
		return p.createReader(new EntityId_t.UserDefinedEntityId(new byte[]{0,0,1}, (byte)0x07), 
				"HelloWorldData_Msg", "HelloWorldData::Msg", new Marshaller() {
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

	private static RTPSWriter createHelloWriter(RTPSParticipant p) {
		 return p.createWriter(new EntityId_t.UserDefinedEntityId(new byte[]{0,0,1}, (byte)0x02), 
				"HelloWorldData_Msg", "HelloWorldData::Msg", new Marshaller() {
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
	
	private static class HelloWorldData {
		long id;
		String message;
		public HelloWorldData(int id, String message) {
			this.id = id;
			this.message = message;
		}
	}
}
