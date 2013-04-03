import java.io.IOException;
import java.nio.ByteBuffer;

import alt.rtps.RTPSParticipant;
import alt.rtps.RTPSReader;
import alt.rtps.RTPSWriter;
import alt.rtps.message.DataEncapsulation;
import alt.rtps.message.data.CDREncapsulation;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;
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
				"HelloWorldData_Msg", "HelloWorldData::Msg", new Marshaller<HelloWorldData>() {
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
						RTPSByteBuffer bb = new RTPSByteBuffer(ByteBuffer.allocate(512));
						bb.write_long(data.id);
						bb.write_string(data.message);
						
						CDREncapsulation cdrEnc = new CDREncapsulation(bb, (short) 0);
						
						return cdrEnc;
					}
				});
	}

	private static RTPSWriter createHelloWriter(RTPSParticipant p) {
		 return p.createWriter(new EntityId_t.UserDefinedEntityId(new byte[]{0,0,1}, (byte)0x02), 
					"HelloWorldData_Msg", "HelloWorldData::Msg", new Marshaller<HelloWorldData>() {
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
					RTPSByteBuffer bb = new RTPSByteBuffer(ByteBuffer.allocate(512));
					bb.write_long(data.id);
					bb.write_string(data.message);
					
					CDREncapsulation cdrEnc = new CDREncapsulation(bb, (short) 0);
					
					return cdrEnc;
				}
			});
	}
	
	private static class HelloWorldData {
		int id;
		String message;
		public HelloWorldData(int id, String message) {
			this.id = id;
			this.message = message;
		}
	}
}
