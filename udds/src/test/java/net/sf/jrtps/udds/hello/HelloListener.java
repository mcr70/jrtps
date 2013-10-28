package net.sf.jrtps.udds.hello;

import java.util.List;

import net.sf.jrtps.udds.DataListener;

public class HelloListener implements DataListener<HelloMessage> {

	@Override
	public void onDataAvailable(List<HelloMessage> samples) {		
		System.out.println("onDataAvailable(): " + samples);
	}

	@Override
	public void onDataDisposed(List<HelloMessage> samples) {
		System.out.println("onDataDisposed(): " + samples);
	}
}
