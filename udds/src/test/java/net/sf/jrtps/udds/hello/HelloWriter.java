package net.sf.jrtps.udds.hello;

import java.net.SocketException;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;


public class HelloWriter {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException, InterruptedException {
		Participant p = new Participant(0, 3); // Create participant; domain 0, participant 3
		
		DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);
		
		for (int i = 0; i < 5; i++) {
			HelloMessage m = new HelloMessage(i, "Hello");
			dw.write(m);
			Thread.sleep(1000);
		}
		
		p.close();
	}
}
