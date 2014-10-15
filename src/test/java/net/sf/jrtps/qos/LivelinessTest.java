package net.sf.jrtps.qos;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.rtps.WriterLivelinessListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;

import org.junit.Assert;
import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class LivelinessTest extends AbstractQosTest {

    /**
     */
    @Test
    public void testManualByTopic() {
        final long LIVELINESS_DURATION = 100;

        QualityOfService qos= new QualityOfService();
        qos.setPolicy(new QosLiveliness(QosLiveliness.Kind.MANUAL_BY_TOPIC, LIVELINESS_DURATION));

        // Create DataWriter
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(2); 
        final CountDownLatch livelinessLostLatch = new CountDownLatch(1);
        final CountDownLatch livelinessRestoredLatch = new CountDownLatch(1);
        
        dr.addWriterListener(new WriterLivelinessListener() {
            @Override
            public void livelinessRestored(PublicationData pd) {
                livelinessRestoredLatch.countDown();
            }
            
            @Override
            public void livelinessLost(PublicationData pd) {
                livelinessLostLatch.countDown();
            }
        });

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw, null, emLatch);

        // Wait for the readers and writer to be matched
        try {
            boolean await = emLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS);
            assertTrue("Entities were not matched in time", await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }

        // Wait for liveliness lost event
        try {
            boolean await = livelinessLostLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS); 
            assertTrue("liveliness was not lost", await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
        
        dw.assertLiveliness();
        
        try {
            boolean await = livelinessRestoredLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS); 
            assertTrue("liveliness was not restored", await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
    }

}
