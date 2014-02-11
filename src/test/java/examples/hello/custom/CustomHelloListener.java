package examples.hello.custom;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.SampleListener;

public class CustomHelloListener implements SampleListener<CustomHelloMessage> {
    @Override
    public void onSamples(List<Sample<CustomHelloMessage>> samples) {
        System.out.println("*** Got samples: " + samples);
    }
}
