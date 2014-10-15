package net.sf.jrtps.qos;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosLifespan;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class LifespanTest extends AbstractQosTest {

    /**
     * Test for LIFESPAN QoS policy.
     *  1.  Create reader, and writer with LIFESPAN of 100ms
     *  2.  wait for entities to be matched
     *  3.  write sample
     *  4.  wait for data to arrive to reader
     *  5.  wait LIFESPAN to expire 
     *  6.  Assert that sample has been removed from reader    
     */
    @Test
    public void testLifeSpanOnReader() {
        final long LIFESPAN_DURATION = 100;

        QualityOfService qos= new QualityOfService();
        qos.setPolicy(new QosLifespan(LIFESPAN_DURATION));

        // Create DataWriter
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);

        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(2); 
        final CountDownLatch dataLatch = new CountDownLatch(1);

        dr.addSampleListener(new SampleListener<HelloMessage>() {
            @Override
            public void onSamples(List<Sample<HelloMessage>> samples) {
                for (int i = 0; i < samples.size(); i++) {
                    dataLatch.countDown();
                }
            }
        });

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw, null, emLatch);

        // Wait for the readers and writer to be matched
        waitFor(emLatch, EMLATCH_WAIT_MILLIS, true);

        dw.write(new HelloMessage(1, "hello"));

        // Wait for reader to receive sample
        waitFor(dataLatch, 1000, true);

        assertEquals(1, dr.getSamples().size()); // assert that we have a sample

        waitFor(2 * LIFESPAN_DURATION);

        assertEquals(0, dr.getSamples().size()); // assert that sample is removed
    }

}
