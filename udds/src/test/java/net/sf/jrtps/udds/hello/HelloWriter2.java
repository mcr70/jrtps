package net.sf.jrtps.udds.hello;

import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;


public class HelloWriter2 {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException, InterruptedException {
		Participant p = new Participant(0, 4); // Create participant; domain 0, participant 4
		
		DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);
		
		List<HelloMessage> msgs = new LinkedList<>();
		for (int i = 0; i < 5; i++) {
			msgs.add(new HelloMessage(i, "hello " + i));
		}
		
		dw.dispose(msgs); // send 10 HelloMessages on one call
		
		System.out.println("\n*** Waiting 2 seconds before closing ***\n");
		Thread.sleep(2000); // Give some time for the recipients to receive before closing
		
		p.close();
	}
}
