package net.sf.jrtps.udds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.OutOfResources;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosHistory.Kind;
import net.sf.jrtps.message.parameter.QosLifespan;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.rtps.ChangeKind;
import net.sf.jrtps.rtps.RTPSParticipant;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.util.Watchdog;

import org.junit.Test;

public class QosTest {
    @Test
    public void testTimeBasedFilter() {
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        
        // Setup QoS. TBF: 100ms, history to prevent historyCache from
        // removing oldest samples.
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosTimeBasedFilter(100));
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
            Thread.sleep(300);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        
        // Fifth sample should emerge
        assertEquals(3, rCache.getSamplesSince(0).size());
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




    @Test
    public <T> void testResourceLimitsMaxSamplesPerInstance() {
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        
        // Setup QoS. ResourceLimits
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosResourceLimits(2,2,1)); // max_s=2, max_i=2, max_s/i=1
        qos.setPolicy(new QosHistory(Kind.KEEP_ALL, 10));
        
        UDDSReaderCache<?> rCache = new UDDSReaderCache<>(null, null, qos, watchdog);
        
        // Add Sample
        rCache.addSample(new Sample(1));
        assertEquals(1, rCache.getSamplesSince(0).size());
        try {
            rCache.addSample(new Sample(2));
            fail("max_samples_per_instance failed");
        }
        catch(OutOfResources oor) {
            // expected
        }
    }


    @Test
    public <T> void testResourceLimitsMaxSamples() {
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        
        // Setup QoS. ResourceLimits
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosResourceLimits(2,-1,-1)); // max_s=2, max_i=-1, max_s/i=-1
        qos.setPolicy(new QosHistory(Kind.KEEP_ALL, 10));
        
        UDDSReaderCache<?> rCache = new UDDSReaderCache<>(null, null, qos, watchdog);
        
        // Add Sample
        rCache.addSample(new Sample(1));
        rCache.addSample(new Sample(2));
        assertEquals(2, rCache.getSamplesSince(0).size());
        try {
            rCache.addSample(new Sample(3));
            fail("max_samples failed");
        }
        catch(OutOfResources oor) {
            // expected
        }
    }

    @Test
    public void testLifespanOnWriter() {
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        
        // Setup QoS. Lifespan: 100ms
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosLifespan(100));
        UDDSWriterCache<?> rCache = new UDDSWriterCache<>(null, null, qos, watchdog);
        
        // Add Sample
        rCache.addSample(new Sample(1));
        assertEquals(1, rCache.getSamplesSince(0).size());
                
        // Wait until Lifespan time has elapsed (+1 ms)
        try {
            Thread.sleep(101);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        
        assertEquals(0, rCache.getSamplesSince(0).size());
    }

    //@Test
    public <T> void testLifespanOnReader() {

        ScheduledThreadPoolExecutor ses = new ScheduledThreadPoolExecutor(10);
        Watchdog watchdog = new Watchdog(ses);
        Guid guid = new Guid(new byte[16]);

        RTPSParticipant p = new RTPSParticipant(guid, 0, -1, ses, 
                new HashMap<GuidPrefix, ParticipantData>(), new Configuration());

        
        // Setup QoS. Lifespan: 100ms
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosLifespan(100));
        UDDSReaderCache<T> rCache = new UDDSReaderCache<>(null, null, new QualityOfService(), watchdog);

        RTPSReader<T> reader = p.createReader(EntityId.UNKNOWN_ENTITY, "test", rCache, new QualityOfService());
        reader.addMatchedWriter(new PublicationData("test", "typeNAme", guid, qos));
        rCache.setRTPSReader((RTPSReader<T>) reader);
        
        // Add Sample
        rCache.addSample(new Sample(guid, null, 1, 1, ChangeKind.WRITE, new Object()));
        assertEquals(1, rCache.getSamplesSince(0).size());
                
        // Wait until Lifespan time has elapsed (+1 ms)
        try {
            Thread.sleep(101);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        
        assertEquals(0, rCache.getSamplesSince(0).size());
    }
}
