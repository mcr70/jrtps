package net.sf.jrtps.udds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosHistory.Kind;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Duration;

import org.junit.Test;

public class QosTest {
    @Test
    public void testTimeBasedFilter() {
        // Setup QoS. TBF: 100ms, history to prevent historycache from
        // removing oldest samples.
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosTimeBasedFilter(new Duration(100)));
        qos.setPolicy(new QosHistory(Kind.KEEP_LAST, 10));  
        UDDSReaderCache<?> rCache = new UDDSReaderCache<>(null, null, qos, null);
        
        // Add some samples. First smaple should succeed, next two should not
        rCache.addSample(new Sample(1));
        assertEquals(1, rCache.getSamplesSince(0).size());
        
        rCache.addSample(new Sample(2));
        rCache.addSample(new Sample(3));
        
        assertEquals(1, rCache.getSamplesSince(0).size());
        
        // Wait until TimeBasedFilter time has elapsed (+1 ms)
        try {
            Thread.sleep(101);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        
        // fourth sample should succeed
        rCache.addSample(new Sample(4));
        assertEquals(2, rCache.getSamplesSince(0).size());
    }
}
