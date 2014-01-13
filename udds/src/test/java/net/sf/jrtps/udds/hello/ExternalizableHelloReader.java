package net.sf.jrtps.udds.hello;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;


public class ExternalizableHelloReader {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
		ExternalizableHelloListener hl = new ExternalizableHelloListener(); // implements DataListener

		Participant p = new Participant(0, 1); // Create participant; domain 0, participant 2

		DataReader<ExternalizableHelloMessage> dr = p.createDataReader(ExternalizableHelloMessage.class);
		dr.addListener(hl);
		
		System.out.println("\n*** Press enter to close Participant ***\n");
		System.in.read();
		
		p.close();
	}
}
