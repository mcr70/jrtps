package net.sf.jrtps.udds;

import java.io.IOException;

public class ParticipantTest {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
		Participant p = new Participant(); // Create participant; domain 0, participant 0
		p.close();
	}
}
