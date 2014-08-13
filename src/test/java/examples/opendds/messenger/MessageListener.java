package examples.opendds.messenger;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.SampleListener;

public class MessageListener implements SampleListener<Message> {

    @Override
    public void onSamples(List<Sample<Message>> samples) {
        for (Sample<Message> sample : samples) {
            System.out.println("Got sample: " + sample.getData());
        }
    }
}
