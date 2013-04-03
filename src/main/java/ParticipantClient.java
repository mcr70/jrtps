import java.io.IOException;

import alt.rtps.RTPSParticipant;
import alt.rtps.RTPSReader;
import alt.rtps.RTPSWriter;
import alt.rtps.message.data.CDREncapsulation;
import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;


public class ParticipantClient {
	public static void main(String[] args) throws IOException {		
		RTPSParticipant p = new RTPSParticipant(0); // Participant to domain 0
		
		Marshaller<HelloWorldData> m = createMarshaller();
		
		//createHelloReader(p, m);
		RTPSWriter w = createHelloWriter(p, m);
		w.createChange(new HelloWorldData(1, "mika"));

		p.start(); // start Participant. Threads will start and initial messages are sent
	}

	private static Marshaller<HelloWorldData> createMarshaller() {
		return new Marshaller<HelloWorldData>() {
			@Override
			public HelloWorldData unmarshall(DataEncapsulation dEnc) {
				CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
				
				RTPSByteBuffer bb = cdrEnc.getBuffer();
				int id = bb.read_long();
				String msg = bb.read_string();
				
				return new HelloWorldData(id, msg);
			}

			@Override
			public DataEncapsulation marshall(HelloWorldData data) {			
				CDREncapsulation cdrEnc = new CDREncapsulation(512);
				RTPSByteBuffer bb = cdrEnc.getBuffer();
				
				bb.write_long(data.id);
				bb.write_string(data.message);
				
				return cdrEnc;
			}
		};
	}

	private static RTPSReader createHelloReader(RTPSParticipant p, Marshaller<HelloWorldData> m) {
		return p.createReader("HelloWorldData_Msg", "HelloWorldData::Msg", m);
	}

	private static RTPSWriter createHelloWriter(RTPSParticipant p, Marshaller<HelloWorldData> m) {
		 return p.createWriter("HelloWorldData_Msg", "HelloWorldData::Msg", m);
	}
	
	private static class HelloWorldData {
		int id;
		String message;
		public HelloWorldData(int id, String message) {
			this.id = id;
			this.message = message;
		}
		
		public String toString() {
			return id + ":" + message;
		}
	}
}
