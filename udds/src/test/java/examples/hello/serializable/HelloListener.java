package examples.hello.serializable;

import java.util.List;

import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;

public class HelloListener implements SampleListener<HelloMessage> {
	@Override
	public void onSamples(List<Sample<HelloMessage>> samples) {
		System.out.println("*** Got samples: " + samples);
	}
}
