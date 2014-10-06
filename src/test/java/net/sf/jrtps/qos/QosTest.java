package net.sf.jrtps.qos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosDurability.Kind;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosPartition;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.transport.mem.MemProvider;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.CommunicationListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;
import net.sf.jrtps.udds.SampleListener;

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
    //@Test
    public void testDeadlineMissed() {
        int DEADLINE_PERIOD = 10;
        
        Configuration cfg1 = new Configuration("/mem-test-1.properties");
        Configuration cfg2 = new Configuration("/mem-test-2.properties");
        
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

    
    @Test
    public void testQosPartition() {
        QosPartition p1 = QosPartition.defaultPartition();
        QosPartition p2 = QosPartition.defaultPartition();
        
        assertTrue(p1.isCompatible(p2));
        assertTrue(p2.isCompatible(p1));
        
        p1 = new QosPartition(new String[]{"p1", "p2", "p3"});
        assertFalse(p1.isCompatible(p2));
        assertFalse(p2.isCompatible(p1));
        
        p2 = new QosPartition(new String[]{"p2"});
        assertTrue(p1.isCompatible(p2));
        assertTrue(p2.isCompatible(p1));
        
        p2 = new QosPartition(new String[]{"p.*"});
        assertTrue(p1.isCompatible(p2));
        assertTrue(p2.isCompatible(p1));
        
        // Note, spec says this should not work; two partitions, both with regexp
        // This is intentional deviation from spec. It is too complex to determine
        // if regular expressions are used or not.
        p1 = new QosPartition(new String[]{"p1.*"});  
        assertTrue(p1.isCompatible(p2));
        assertTrue(p2.isCompatible(p1));
    }


    /**
     * Test for durability QoS policy.
     *  1.   Create two readers; VOLATILE and TRANSIENT_LOCAL and one writer; TRANSIENT_LOCAL.
     *  2.   write 3 samples.
     *  3.   wait for entities to be matched
     *  4.a  if VOLATILE reader receives any data --> FAIL
     *  4.b  wait for TRANSIENT_LOCAL reader to receive 3 samples in time.
     *       If timeout occurs --> FAIL 
     */
    //@Test
    public void testDurability() {
        Configuration cfg1 = new Configuration("/mem-test-1.properties");
        Configuration cfg2 = new Configuration("/mem-test-2.properties");
        
        MemProvider mp = new MemProvider(cfg1);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);

        // Create two participants; one for readers, one for writer
        Participant p1 = new Participant(0,0, null, cfg1);
        Participant p2 = new Participant(0,0, null, cfg2);

        // Create DURABILITY policies to be used by entities
        QualityOfService qosVolatile = new QualityOfService();
        qosVolatile.setPolicy(new QosDurability(Kind.VOLATILE));

        QualityOfService qosTRLocal= new QualityOfService();
        qosTRLocal.setPolicy(new QosDurability(Kind.TRANSIENT_LOCAL));
        
        QualityOfService qosWriter = new QualityOfService();
        qosWriter.setPolicy(new QosDurability(Kind.TRANSIENT_LOCAL));

        // Create DataWriter and write some samples
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qosWriter);
        
        dw.write(new HelloMessage(1, "hello"));
        dw.write(new HelloMessage(2, "hello"));
        dw.write(new HelloMessage(3, "hello"));
        
        // Create DataReaders
        DataReader<HelloMessage> drVolatile = p1.createDataReader(HelloMessage.class, qosVolatile);
        DataReader<HelloMessage> drTRLocal = p1.createDataReader(HelloMessage.class, qosTRLocal);
        
        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(3); 
        final CountDownLatch trDataLatch = new CountDownLatch(3);
        
        drVolatile.addSampleListener(new SampleListener<HelloMessage>() {
            @Override
            public void onSamples(List<Sample<HelloMessage>> samples) {
                Assert.fail("Volatile reader should not have received a sample");
            }
        });
        
        drTRLocal.addSampleListener(new SampleListener<HelloMessage>() {
            @Override
            public void onSamples(List<Sample<HelloMessage>> samples) {
                for (int i = 0; i < samples.size(); i++) {
                    trDataLatch.countDown();
                }
            }
        });
        
        drVolatile.addCommunicationListener(new CommunicationListener<PublicationData>() {            
            @Override
            public void deadlineMissed(KeyHash instanceKey) {
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
        
        drTRLocal.addCommunicationListener(new CommunicationListener<PublicationData>() {            
            @Override
            public void deadlineMissed(KeyHash instanceKey) {
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

        // Wait for the readers and writer to be matched
        try {
            emLatch.await();
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
        
        // Wait for transient local reader to receive all the samples
        try {
            boolean await = trDataLatch.await(1000, TimeUnit.MILLISECONDS); 
            assertTrue("Did not receive samples for transient local reader on time: " + trDataLatch.getCount(), await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
                
        p1.close();
        p2.close();
    }


    /**
     * Test for HISTORY QoS policy.
     *  1.  Create TRANSIENT_LOCAL reader TRANSIENT_LOCAL writer with HISTORY(2) 
     *  2.  write 3 samples to same instance.
     *  3.  wait for entities to be matched
     *  4.  wait for TRANSIENT_LOCAL reader to receive _3_ samples.
     *      If timeout occurs --> SUCCESS
     */
    //@Test
    public void testHistory() {
        Configuration cfg1 = new Configuration("/mem-test-1.properties");
        Configuration cfg2 = new Configuration("/mem-test-2.properties");
        
        MemProvider mp = new MemProvider(cfg1);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);

        // Create two participants; one for readers, one for writer
        Participant p1 = new Participant(0,0, null, cfg1);
        Participant p2 = new Participant(0,0, null, cfg2);

        QualityOfService qos= new QualityOfService();
        qos.setPolicy(new QosDurability(Kind.TRANSIENT_LOCAL));
        qos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 2));

        // Create DataWriter and write some samples
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);
        
        dw.write(new HelloMessage(1, "hello"));
        dw.write(new HelloMessage(1, "hello"));
        dw.write(new HelloMessage(1, "hello"));
        
        // Create DataReaders
        DataReader<HelloMessage> drTRLocal = p1.createDataReader(HelloMessage.class, qos);
        
        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(2); 
        final CountDownLatch trDataLatch = new CountDownLatch(3);
        drTRLocal.addSampleListener(new SampleListener<HelloMessage>() {
            @Override
            public void onSamples(List<Sample<HelloMessage>> samples) {
                for (int i = 0; i < samples.size(); i++) {
                    trDataLatch.countDown();
                }
            }
        });
                
        drTRLocal.addCommunicationListener(new CommunicationListener<PublicationData>() {            
            @Override
            public void deadlineMissed(KeyHash instanceKey) {
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

        // Wait for the readers and writer to be matched
        try {
            emLatch.await();
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
        
        // Wait for transient local reader to receive all the samples
        try {
            boolean await = trDataLatch.await(1000, TimeUnit.MILLISECONDS); 
            assertFalse("Received more than 2 samples" + trDataLatch.getCount(), await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
                
        p1.close();
        p2.close();
    }
}
