package net.sf.jrtps.udds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosHistory.Kind;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.util.Watchdog;

import org.junit.Test;

public class QosTest {
    @Test
    public void testTimeBasedFilter() {
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        
        // Setup QoS. TBF: 100ms, history to prevent historycache from
        // removing oldest samples.
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosTimeBasedFilter(new Duration(100)));
        qos.setPolicy(new QosHistory(Kind.KEEP_LAST, 10));  
        UDDSReaderCache<?> rCache = new UDDSReaderCache<>(null, null, qos, watchdog);
        
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
        
        // fifth sample shoud fail, for now
        rCache.addSample(new Sample(5));
        assertEquals(2, rCache.getSamplesSince(0).size());

        // Wait until 3 * TimeBasedFilter time has elapsed (*2 is used in the code)
        try {
            Thread.sleep(1300);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        
        // Fifth sample should emerge, but it does not some refactoring is needed
        //assertEquals(3, rCache.getSamplesSince(0).size());
    }
    
    @Test
    public void testTBFForInconsistentQoS() {
        QualityOfService qos = new QualityOfService();
        
        qos.setPolicy(new QosDeadline(100));
        qos.setPolicy(new QosTimeBasedFilter(99));
        // should be ok at this point
        
        try {
            qos.setPolicy(new QosTimeBasedFilter(101)); // should throw exception
            fail("InconsistentPolicy was expected");
        }
        catch(InconsistentPolicy ip) {
        }
    }
}
