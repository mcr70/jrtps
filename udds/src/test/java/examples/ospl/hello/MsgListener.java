package examples.ospl.hello;

import java.util.List;

import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;

public class MsgListener implements SampleListener<Msg> {

	@Override
	public void onSamples(List<Sample<Msg>> samples) {
		System.out.println("Got samples: " + samples);
	}
}
