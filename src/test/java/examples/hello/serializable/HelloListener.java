package examples.hello.serializable;

import java.util.List;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.SampleListener;
import net.sf.jrtps.udds.WriterListener;

public class HelloListener implements SampleListener<HelloMessage>, WriterListener {
    @Override
    public void onSamples(List<Sample<HelloMessage>> samples) {
        System.out.println("*** Got samples: " + samples);
    }

    @Override
    public void livelinessLost(PublicationData pd) {
        System.out.println("*** livelinessLost: " + pd);
    }

    @Override
    public void writerMatched(PublicationData pd) {
        System.out.println("*** writerMatched: " + pd);
    }

    @Override
    public void inconsistentQoS(PublicationData pd) {
        System.out.println("*** inconsistentQos: " + pd);
    }
}
