package net.sf.jrtps.udds.hello;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;


public class HelloReader {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
		HelloListener hl = new HelloListener(); // implements DataListener

		Participant p = new Participant(0, 2); // Create participant; domain 0, participant 2

		DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class);
		dr.addListener(hl);
		
		System.out.println("\n*** Press enter to close Participant ***\n");
		System.in.read();
		
		p.close();
	}
}
