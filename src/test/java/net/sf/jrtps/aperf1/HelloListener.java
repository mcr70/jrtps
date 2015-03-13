package net.sf.jrtps.aperf1;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.SampleListener;

public class HelloListener implements SampleListener<Hello> {
	int count = 0;
	long l1 = 0;
	
    @Override
    public void onSamples(List<Sample<Hello>> samples) {
    	if (count == 0) {
    		l1 = System.currentTimeMillis();
    	}
    	
    	count += samples.size();
    	
    	if (count >= 10) {
    		long l2 = System.currentTimeMillis();
    		System.out.println(count + " samples in " + (l2-l1) + " ms");
    		count = 0;
    		l1 = 0;
    	}
    	
    	for (Sample<Hello> sample : samples) {
            System.out.println("Got sample: " + sample.getData());
        }
    }
}
