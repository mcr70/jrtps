package net.sf.jrtps.qos;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosDurability.Kind;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Test;

import examples.hello.serializable.HelloMessage;


public class HistoryTest extends AbstractQosTest {

    /**
     * Test for HISTORY QoS policy.
     *  1.  Create TRANSIENT_LOCAL reader TRANSIENT_LOCAL writer with HISTORY(2) 
     *  2.  write 3 samples to same instance.
     *  3.  wait for entities to be matched
     *  4.  wait for TRANSIENT_LOCAL reader to receive _3_ samples.
     *      If timeout occurs --> SUCCESS
     */
    @Test
    public void testHistory() {
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

        addCommunicationListener(drTRLocal, null, emLatch);
        addCommunicationListener(dw, null, emLatch);

        // Wait for the readers and writer to be matched
        waitFor(emLatch, EMLATCH_WAIT_MILLIS, true);

        // Wait for transient local reader to receive all the samples
        waitFor(trDataLatch, 1000, false);
    }
}
