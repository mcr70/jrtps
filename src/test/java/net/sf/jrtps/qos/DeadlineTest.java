package net.sf.jrtps.qos;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;

import org.junit.Assert;
import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class DeadlineTest extends AbstractQosTest {


    /**
     * Test for deadline missed event to occur on both reader and writer.
     */
    @Test
    public void testDeadlineMissed() {
        int DEADLINE_PERIOD = 10;

        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosDeadline(new Duration(DEADLINE_PERIOD)));

        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);

        final CountDownLatch emLatch = new CountDownLatch(2); // Latch used to synchronize on entity matched
        final CountDownLatch dlLatch = new CountDownLatch(2); // Latch used to wait for deadlines to occur

        addCommunicationListener(dr, dlLatch, emLatch);
        addCommunicationListener(dw, dlLatch, emLatch);

        try {
            boolean await = emLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS); // Wait for the reader and writer to be matched
            assertTrue("Entities were not matched in time", await);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }

        // Write a message once reader and writer has been matched

        HelloMessage m = new HelloMessage(1 , "Hello ");
        dw.write(m); 

        // If we do not write next message within deadline period, deadline missed should happen

        try {
            boolean await = dlLatch.await(LATCH_WAIT_SECS, TimeUnit.SECONDS);

            assertTrue(await); // check, that deadline missed was called 
        } catch (InterruptedException e) {
            Assert.fail("Interrupted");
        }
    }

}
