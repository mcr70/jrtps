package net.sf.jrtps.transport.mem;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.SampleListener;

public class HelloListener implements SampleListener<HelloMessage>{
    @Override
    public void onSamples(List<Sample<HelloMessage>> samples) {
        System.out.println("*** Got samples: " + samples);
    }
}
