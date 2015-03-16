package examples.rti.hello;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.SampleListener;

public class HelloListener implements SampleListener<Hello> {

    @Override
    public void onSamples(List<Sample<Hello>> samples) {
        for (Sample<Hello> sample : samples) {
            System.out.println("Got sample: " + sample.getData());
        }
    }
}
