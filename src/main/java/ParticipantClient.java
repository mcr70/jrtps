import java.io.IOException;

import alt.rtps.RTPSParticipant;


public class ParticipantClient {
	public static void main(String[] args) throws IOException {		
		RTPSParticipant p = new RTPSParticipant(0); // Participant to domain 0
		p.start(); // start Participant. Threads will start and initial messages are sent
	}
}
