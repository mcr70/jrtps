package examples.hello.externalizable;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.SampleListener;

public class ExternalizableHelloListener implements SampleListener<ExternalizableHelloMessage> {
    @Override
    public void onSamples(List<Sample<ExternalizableHelloMessage>> samples) {
        System.out.println("*** Got samples: " + samples);
    }
}
