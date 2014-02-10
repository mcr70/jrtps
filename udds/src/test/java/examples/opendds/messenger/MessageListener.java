package examples.opendds.messenger;

import java.util.List;

import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;

public class MessageListener implements SampleListener<Message> {

    @Override
    public void onSamples(List<Sample<Message>> samples) {
        System.out.println("Got samples: " + samples);
    }
}
