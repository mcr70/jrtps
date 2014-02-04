package examples.hello.externalizable;

import java.util.List;

import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;

public class ExternalizableHelloListener implements SampleListener<ExternalizableHelloMessage> {
	@Override
	public void onSamples(List<Sample<ExternalizableHelloMessage>> samples) {
		System.out.println("*** Got samples: " + samples);
	}
}
