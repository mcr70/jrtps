package net.sf.udds.hello;

import java.net.SocketException;

import net.sf.udds.DataWriter;
import net.sf.udds.Participant;

public class HelloWriter {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException, InterruptedException {
		Participant p = new Participant(0, 3); // Create participant; domain 0, participant 3
		
		HelloMessage m = new HelloMessage(1, "Hello");
		DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);
		
		for (int i = 0; i < 10; i++) {
			dw.write(m);
			Thread.sleep(1000);
		}
	}
}
