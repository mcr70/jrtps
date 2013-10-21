package net.sf.jrtps.udds.hello;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.udds.DataListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;
import net.sf.jrtps.udds.Sample;


public class HelloReader {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
		HelloListener hl = new HelloListener(); // implements DataListener
		
		Participant p = new Participant(0, 2); // Create participant; domain 0, participant 2

		DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class);
		dr.addListener(hl);
		
		System.out.println("Press enter to close Participant");
		System.in.read();
		
		p.close();
	}
}
