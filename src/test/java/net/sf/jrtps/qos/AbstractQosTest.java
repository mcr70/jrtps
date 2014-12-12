package net.sf.jrtps.qos;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rtps.WriterLivelinessListener;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.transport.mem.MemProvider;
import net.sf.jrtps.udds.CommunicationListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.EntityFactory;
import net.sf.jrtps.udds.Participant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import examples.hello.serializable.HelloMessage;

public class AbstractQosTest {
    static final long LATCH_WAIT_MILLIS = 2000;
    
    protected static Configuration cfg1 = new Configuration("/mem-test-1.properties");
    protected static Configuration cfg2 = new Configuration("/mem-test-2.properties");

    static {
        MemProvider mp = new MemProvider(cfg1);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);
    }

    protected Participant p1;
    protected Participant p2;

    @Before
    public void init() {
        EntityFactory ef = new TEntityFactory();
        // Create two participants; one reader, one for writer
        p1 = new Participant(0,0, ef, cfg1);
        p2 = new Participant(0,0, ef, cfg2);
    }

    @After 
    public void destroy() {
        p1.close();        
        p2.close();
    }


    void addCommunicationListener(DataReader<HelloMessage> dr, final CountDownLatch dlLatch, final CountDownLatch emLatch) {
        dr.addCommunicationListener(new CommunicationListener<PublicationData>() {            
            @Override
            public void deadlineMissed(KeyHash instanceKey) {
                dlLatch.countDown();
            }
            @Override
            public void inconsistentQoS(PublicationData ed) {
                Assert.fail("Inconsistent QoS not expected");
            }            
            @Override
            public void entityMatched(PublicationData ed) {
                emLatch.countDown();
            }
        });    
    }

    void addCommunicationListener(DataWriter<HelloMessage> dw, final CountDownLatch dlLatch, final CountDownLatch emLatch) {
        dw.addCommunicationListener(new CommunicationListener<SubscriptionData>() {            
            @Override
            public void deadlineMissed(KeyHash instanceKey) {
                dlLatch.countDown();
            }

            @Override
            public void inconsistentQoS(SubscriptionData ed) {
                Assert.fail("Inconsistent QoS not expected");
            }
            @Override
            public void entityMatched(SubscriptionData ed) {
                emLatch.countDown();
            }
        });
    }

    void addLivelinessListener(DataReader<HelloMessage> dr, final CountDownLatch restoredLatch, final CountDownLatch lostLatch) {
        dr.addWriterListener(new WriterLivelinessListener() {
            @Override
            public void livelinessRestored(PublicationData pd) {
                restoredLatch.countDown();
            }
            
            @Override
            public void livelinessLost(PublicationData pd) {
                lostLatch.countDown();
            }
        });
    }
    
    
    void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Assert.fail("interrupted");
        }
    }
    
    void waitFor(CountDownLatch latch, long durationInMillis, boolean countToZeroExpected) {
        try {
            boolean await = latch.await(durationInMillis, TimeUnit.MILLISECONDS); 
            Assert.assertEquals(countToZeroExpected, await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }        
    }
}
