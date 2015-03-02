package net.sf.jrtps.qos;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosOwnership.Kind;
import net.sf.jrtps.message.parameter.QosOwnershipStrength;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;
import net.sf.jrtps.udds.SampleListener;
import examples.hello.serializable.HelloMessage;


public class OwnershipTest extends AbstractQosTest {

    /**
     * Test for OWNERSHIP QoS policy.
     *  1.  Create reader and 2 writers with EXCLUSIVE ownership. 
     *  2.  wait for entities to be matched
     *  3.  write a sample with weaker writer
     *  4.  wait for data to arrive to reader. Assert we got 1 sample.
     *  5.  write a sample with stronger writer
     *  6.  wait for data to arrive to reader. Assert we got 2 samples.
     *  7.  write a sample with weaker writer
     *  8.  assert that we have received a total of 2 samples.
     */
    //@Test
    public void testOwnership() {
    	Configuration cfg3 = new Configuration("/mem-test-3.properties");
    	Participant p3 = new Participant(0, -1, null, cfg3);
    	
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
        DataWriter<HelloMessage> dw2 = p3.createDataWriter(HelloMessage.class, qos2);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qosDr);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(6); 

        TestListener<HelloMessage> listener = new TestListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);

        // Write a sample with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LATCH_WAIT_MILLIS, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Write a sample with 'stronger' writer
        listener.resetLatch(1);
        dw2.write(new HelloMessage(1, "s1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LATCH_WAIT_MILLIS, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
        
        
        // Write a sample with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w3 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, 300, false);

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
    //@Test
    public void testOwnershipWhenWriterIsClosed() {
        //Configuration cfg3 = new Configuration("/mem-test-3.properties");

    	// TODO: We need a third Participant for dw2
    	
    	final int LEASE_DURATION = 200;
        
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
        final CountDownLatch emLatch = new CountDownLatch(4); 

        TestListener<HelloMessage> listener = new TestListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);

        // Write a sample with 'stronger' writer
        listener.resetLatch(1);
        dw2.write(new HelloMessage(1, "w1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LATCH_WAIT_MILLIS, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Write two samples with 'weaker' writer
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "s1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, false);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        dw2.close(); // Close 'stronger' writer 
        waitFor(LEASE_DURATION); // wait for close to propagate to reader
        
        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample

        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w1 hello")); // Write with 'weaker' writer

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
    }
    
    
    /**
     * Test for OWNERSHIP QoS policy.
     *  1.  Create reader and 2 writers with EXCLUSIVE ownership. 
     *      Stronger writer has a lease_duration set.
     *  2.  wait for entities to be matched
     *  3.  write a sample with stronger writer
     *  4.  wait for data to arrive to reader. Assert we got the sample.
     *  5.  wait 2 * LEASE_DURATION (liveliness lost for stronger writer)
     *  6.  write a sample with weaker writer
     *  8.  wait for data to arrive to reader. 
     */
    //@Test
    public void testOwnershipWithLiveliness() {
        final int LEASE_DURATION = 200;
        
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
        final CountDownLatch emLatch = new CountDownLatch(4); 

        TestListener<HelloMessage> listener = new TestListener<>();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);

        // Wait for the reader and writers to be matched
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);

        // Write a sample with 'stronger' writer
        listener.resetLatch(1);
        dw2.write(new HelloMessage(1, "w1 hello"));

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LATCH_WAIT_MILLIS, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have 1 sample
        
        // Let 'stronger' writer miss liveliness assertion
        waitFor(2 * LEASE_DURATION); // Wait for liveliness to expire
        
        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "w1 hello")); // Write with 'weaker' writer

        // Wait for reader to receive sample
        waitFor(listener.dataLatch, LEASE_DURATION, true);

        assertEquals(2, dr.getSamples().size()); // assert that we have 2 samples
    }

    //@Test
    public void testOwnershipWithDeadline() {
        int DEADLINE_PERIOD = 10;

        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos.setPolicy(new QosDeadline(new Duration(DEADLINE_PERIOD)));

        QualityOfService qos1 = new QualityOfService();
        qos1.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos1.setPolicy(new QosOwnershipStrength(1));
        qos1.setPolicy(new QosDeadline(new Duration(DEADLINE_PERIOD)));
        
        QualityOfService qos2 = new QualityOfService();
        qos2.setPolicy(new QosOwnership(Kind.EXCLUSIVE));
        qos2.setPolicy(new QosOwnershipStrength(2));
        qos2.setPolicy(new QosDeadline(DEADLINE_PERIOD));

        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);
        DataWriter<HelloMessage> dw1 = p2.createDataWriter(HelloMessage.class, qos1);
        DataWriter<HelloMessage> dw2 = p2.createDataWriter(HelloMessage.class, qos2);
        
        final CountDownLatch emLatch = new CountDownLatch(4); // Latch used to synchronize on entity matched
        final CountDownLatch dlLatch = new CountDownLatch(1); // Latch used to wait for deadlines to occur

        addCommunicationListener(dr, dlLatch, emLatch);
        addCommunicationListener(dw1, null, emLatch);
        addCommunicationListener(dw2, null, emLatch);
        
        TestListener<HelloMessage> sampleListener = new TestListener<>();
        dr.addSampleListener(sampleListener);
        
        waitFor(emLatch, LATCH_WAIT_MILLIS, true); // Wait for the entities to be matched

        // Write a sample with stronger writer
        HelloMessage m = new HelloMessage(1 , "Hello ");
        dw2.write(m); 

        sampleListener.resetLatch(1);
        waitFor(sampleListener.dataLatch, LATCH_WAIT_MILLIS, true); // Wait for sample to be received
        
        // If we do not write next message within deadline period, deadline missed should happen
        waitFor(dlLatch, LATCH_WAIT_MILLIS, true);

        // Write a sample with weaker writer
        sampleListener.resetLatch(1);
        dw1.write(m);
        
        waitFor(sampleListener.dataLatch, LATCH_WAIT_MILLIS, true); // Wait for sample to be received
    }
    
    
    private class TestListener<T> implements SampleListener<T> {
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
