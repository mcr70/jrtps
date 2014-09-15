package net.sf.jrtps.qos;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.transport.mem.MemProvider;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.CommunicationListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

import org.junit.Assert;
import org.junit.Test;

import examples.hello.serializable.HelloMessage;
/**
 * Tests for QualityOfService functionality.
 * @author mcr70
 */
public class QosTest {
    /**
     * Test for deadline missed event to occur on both reader and writer.
     */
    @Test
    public void testDeadlineMissed() {
        int DEADLINE_PERIOD = 10;
        
        Configuration cfg1 = new Configuration("/net/sf/jrtps/qos/qos-1.properties");
        Configuration cfg2 = new Configuration("/net/sf/jrtps/qos/qos-2.properties");
        
        MemProvider mp = new MemProvider(cfg1);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);

        // Create two participants; one reader, one for writer
        Participant p1 = new Participant(0,0, null, cfg1);
        Participant p2 = new Participant(0,0, null, cfg2);
        
        QualityOfService qos = new QualityOfService();
        try {
            qos.setPolicy(new QosDeadline(new Duration(DEADLINE_PERIOD)));
        } catch (InconsistentPolicy e1) {
            Assert.fail("InconsistentPolicy");
        }
        
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);
        
        final CountDownLatch emLatch = new CountDownLatch(2); // Latch used to synchronize on entity matched
        final CountDownLatch dlLatch = new CountDownLatch(2); // Latch used to wait for deadlines to occur
        
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

        try {
            emLatch.await(); // Wait for the reader and writer to be matched
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
        
        // Write a message once reader and writer has been matched
        
        HelloMessage m = new HelloMessage(1 , "Hello ");
        dw.write(m); 

        // If we do not write next message within deadline period, deadline missed should happen
        
        try {
            boolean await = dlLatch.await(1000, TimeUnit.MILLISECONDS);
            
            assertTrue(await); // check, that deadline missed was called 
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
                
        p1.close();
        p2.close();
    }

}
