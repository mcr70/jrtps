package net.sf.jrtps.qos;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosDurability.Kind;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Assert;
import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class DurabilityTest extends AbstractQosTest {

    /**
     * Test for durability QoS policy.
     *  1.   Create two readers; VOLATILE and TRANSIENT_LOCAL and one writer; TRANSIENT_LOCAL.
     *  2.   write 3 samples.
     *  3.   wait for entities to be matched
     *  4.a  if VOLATILE reader receives any data --> FAIL
     *  4.b  wait for TRANSIENT_LOCAL reader to receive 3 samples in time.
     *       If timeout occurs --> FAIL 
     */
    @Test
    public void testDurability() {
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

        addCommunicationListener(drVolatile, null, emLatch);
        addCommunicationListener(drTRLocal, null, emLatch);
        addCommunicationListener(dw, null, emLatch);

        // Wait for the readers and writer to be matched
        try {
            boolean await = emLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS);
            assertTrue("Entities were not matched in time", await);
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
    }


}
