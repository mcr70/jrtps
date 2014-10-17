package net.sf.jrtps.qos;

import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;

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

        waitFor(emLatch, LATCH_WAIT_MILLIS, true);

        // Write a message once reader and writer has been matched

        HelloMessage m = new HelloMessage(1 , "Hello ");
        dw.write(m); 

        // If we do not write next message within deadline period, deadline missed should happen
        waitFor(dlLatch, LATCH_WAIT_MILLIS, true);
    }

}
