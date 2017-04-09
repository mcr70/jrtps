package net.sf.jrtps.qos;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Test;

import examples.hello.serializable.HelloMessage;

public class DestinationOrderTest extends AbstractQosTest {
    /**
     * Test for DestionationOrder QoS policy.
     */
    @Test
    public void testBySourceTimestamp() {
        executeTest(QosDestinationOrder.Kind.BY_SOURCE_TIMESTAMP, 2);
    }
    
    /**
     * Test for DestionationOrder QoS policy.
     */
    @Test
    public void testByReceptionTimestamp() {
        executeTest(QosDestinationOrder.Kind.BY_RECEPTION_TIMESTAMP, 3);
    }

    private void executeTest(QosDestinationOrder.Kind kind, int count) {      
        QualityOfService qos= new QualityOfService();
        qos.setPolicy(new QosDestinationOrder(kind));
        qos.setPolicy(new QosHistory(10));
        // Create DataReader
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);

        // Create DataWriter
        TDataWriter<HelloMessage> dw1 = (TDataWriter<HelloMessage>) p2.createDataWriter(HelloMessage.class, qos);

        // Latch used to synchronize on entity matched
        final CountDownLatch emLatch = new CountDownLatch(2); 
        TSampleListener listener = new TSampleListener();
        dr.addSampleListener(listener);

        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw1, null, emLatch);

        // Wait for the readers and writer to be matched
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);

        listener.resetLatch(1);
        dw1.write(new HelloMessage(1, "hello"), 10);
        waitFor(listener.dLatch, LATCH_WAIT_MILLIS, true);
        assertEquals(1, dr.getSamples().size());

        listener.resetLatch(count - 1);
        dw1.write(new HelloMessage(1, "hello"), 9);
        dw1.write(new HelloMessage(1, "hello"), 11);

        waitFor(listener.dLatch, LATCH_WAIT_MILLIS, true);
        assertEquals(count, dr.getSamples().size());
    }

    private class TSampleListener implements SampleListener<HelloMessage> {
        CountDownLatch dLatch;
        
        void resetLatch(int count) {
            dLatch = new CountDownLatch(count);
        }
        
        @Override
        public void onSamples(List<Sample<HelloMessage>> samples) {
        	dLatch.countDown();
        }
    }
}
