package net.sf.udds.hello;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.sf.udds.DataListener;
import net.sf.udds.DataReader;
import net.sf.udds.Participant;
import net.sf.udds.Sample;

public class HelloReader implements DataListener<HelloMessage> {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
		HelloReader tc = new HelloReader(); // implements DataListener
		
		Participant p = new Participant(0, 2); // Create participant; domain 0, participant 2

		DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class);
		dr.addListener(tc);
		
		System.out.println("Press enter to take samples");
		System.in.read();
		List<Sample<HelloMessage>> result = dr.take(); // Clears readers cache
		System.out.println(result);
		
		p.close();
	}


	long startTime = 0;
	long endTime = 0;
	int objectCount = 0;
	LinkedList<HelloMessage> objects = new LinkedList<>();
	@Override
	public void onDataAvailable(List<HelloMessage> samples) {
		
		System.out.println("onDataAvailable(): " + samples);
		
		objectCount++;
	}
}
