package net.sf.jrtps.qos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosOwnership.Kind;
import net.sf.jrtps.message.parameter.QosOwnershipStrength;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Assert;
import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class OwnershipTest extends AbstractQosTest {

    /**
     * Test for OWNERSHIP QoS policy.
     *  1.  Create reader and 2 writers with EXCLUSIVE ownership. 
     *  2.  wait for entities to be matched
     *  3.  write 2 samples with weaker writer
     *  4.  wait for data to arrive to reader. Assert we got 2 samples.
     *  5.  write 2 samples with stronger writer
     *  6.  wait for data to arrive to reader. Assert we got 2 samples.
     *  7.  write 2 samples with weaker writer
     *  8.  wait for data to arrive to reader. Assert we got 0 samples.
     *  9.  assert that we have received a total of 4 samples.
     */
    @Test
    public void testOwnership() {
        QualityOfService qosDr = new QualityOfService();
        qosDr.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qosDr.setPolicy(new QosHistory(10)); // Keep all the samples we write in this test
        
        QualityOfService qos1 = new QualityOfService();
        qos1.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos1.setPolicy(new QosOwnershipStrength(1));

        QualityOfService qos2 = new QualityOfService();
        qos2.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos2.setPolicy(new QosOwnershipStrength(2));

        // Create DataWriters
        DataWriter<HelloMessage> dw1 = p2.createDataWriter(HelloMessage.class, qos1);
        DataWriter<HelloMessage> dw2 = p2.createDataWriter(HelloMessage.class, qos2);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qosDr);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(3); 

        TestSampleListener<HelloMessage> listener = new TestSampleListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, EMLATCH_WAIT_MILLIS, true);

        // Write two samples with 'weaker' writer
        listener.resetLatch(2);
        dw1.write(new HelloMessage(1, "w1 hello"));
        dw1.write(new HelloMessage(1, "w2 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 1000, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
        
        // Write two samples with 'stronger' writer
        listener.resetLatch(2);
        dw2.write(new HelloMessage(1, "s1 hello"));
        dw2.write(new HelloMessage(1, "s2 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 1000, true);

        assertEquals(4, dr.getSamples().size()); // assert that we have 4 samples
        
        
        // Write two samples with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w3 hello"));
        dw1.write(new HelloMessage(1, "w4 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 1000, false);

        assertEquals(4, dr.getSamples().size()); // assert that we have 4 samples
    }

    

    /**
     * Test for OWNERSHIP QoS policy.
     *  1.  Create reader and 2 writers with EXCLUSIVE ownership. 
     *  2.  wait for entities to be matched
     *  3.  write 2 samples with weaker writer
     *  4.  wait for data to arrive to reader. Assert we got 2 samples.
     *  5.  write 2 samples with stronger writer
     *  6.  wait for data to arrive to reader. Assert we got 2 samples.
     *  7.  write 2 samples with weaker writer
     *  8.  wait for data to arrive to reader. Assert we got 0 samples.
     *  9.  assert that we have received a total of 4 samples.
     */
    @Test
    public void testOwnershipWhenWriterIsClosed() {
        final int LEASE_DURATION = 1000;
        
        QualityOfService qosDr = new QualityOfService();
        qosDr.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qosDr.setPolicy(new QosHistory(10)); // Keep all the samples we write in this test
        
        QualityOfService qos1 = new QualityOfService();
        qos1.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos1.setPolicy(new QosOwnershipStrength(1));

        QualityOfService qos2 = new QualityOfService();
        qos2.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos2.setPolicy(new QosOwnershipStrength(2));
        //qos2.setPolicy(new QosLiveliness(LEASE_DURATION));

        // Create DataWriters
        DataWriter<HelloMessage> dw1 = p2.createDataWriter(HelloMessage.class, qos1);
        DataWriter<HelloMessage> dw2 = p2.createDataWriter(HelloMessage.class, qos2);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qosDr);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(3); 

        TestSampleListener<HelloMessage> listener = new TestSampleListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, EMLATCH_WAIT_MILLIS, true);
        try {
            boolean await = emLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS);
            assertTrue("Entities were not matched in time", await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }

        // Write a sample with 'stronger' writer
        listener.resetLatch(1);
        dw2.write(new HelloMessage(1, "w1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 1000, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Write two samples with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "s1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, false);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        dw2.close(); // Close 'stronger' writer 

        waitFor(2 * LEASE_DURATION); // Wait for liveliness to expire
        
        assertEquals(1, dr.getSamples().size()); // assert that we have 1 samples

        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w1 hello")); // Write with 'weaker' writer

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
    }
    
    
    /**
     * Test for OWNERSHIP QoS policy.
     *  1.  Create reader and 2 writers with EXCLUSIVE ownership. 
     *  2.  wait for entities to be matched
     *  3.  write 2 samples with weaker writer
     *  4.  wait for data to arrive to reader. Assert we got 2 samples.
     *  5.  write 2 samples with stronger writer
     *  6.  wait for data to arrive to reader. Assert we got 2 samples.
     *  7.  write 2 samples with weaker writer
     *  8.  wait for data to arrive to reader. Assert we got 0 samples.
     *  9.  assert that we have received a total of 4 samples.
     */
    @Test
    public void testOwnershipWithLiveliness() {
        final int LEASE_DURATION = 1000;
        
        QualityOfService qosDr = new QualityOfService();
        qosDr.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qosDr.setPolicy(new QosHistory(10)); // Keep all the samples we write in this test
        
        QualityOfService qos1 = new QualityOfService();
        qos1.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos1.setPolicy(new QosOwnershipStrength(1));

        QualityOfService qos2 = new QualityOfService();
        qos2.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos2.setPolicy(new QosOwnershipStrength(2));
        qos2.setPolicy(new QosLiveliness(QosLiveliness.Kind.MANUAL_BY_TOPIC, LEASE_DURATION));

        // Create DataWriters
        DataWriter<HelloMessage> dw1 = p2.createDataWriter(HelloMessage.class, qos1);
        DataWriter<HelloMessage> dw2 = p2.createDataWriter(HelloMessage.class, qos2);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qosDr);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(3); 

        TestSampleListener<HelloMessage> listener = new TestSampleListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, EMLATCH_WAIT_MILLIS, true);

        // Write a sample with 'stronger' writer
        listener.resetLatch(1);
        dw2.write(new HelloMessage(1, "w1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 1000, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Write sample with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "s1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, false);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Let 'stronger' writer miss liveliness assertion
        CountDownLatch llLatch = new CountDownLatch(1);
        //waitFor(llLatch, 2 * LEASE_DURATION, true); // Wait for liveliness to expire
        waitFor(2 * LEASE_DURATION); // Wait for liveliness to expire
        
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w1 hello")); // Write with 'weaker' writer

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
    }

    
    private class TestSampleListener<T> implements SampleListener<T> {
        CountDownLatch dataLatch;
        
        void resetLatch(int count) {
            dataLatch = new CountDownLatch(count);
        }
        
        @Override
        public void onSamples(List<Sample<T>> samples) {
            dataLatch.countDown();
        }
    }
}
