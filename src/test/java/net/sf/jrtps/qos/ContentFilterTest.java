package net.sf.jrtps.qos;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosDurability.Kind;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.ContentFilter;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

import org.junit.Test;

import examples.hello.serializable.HelloMessage;

public class ContentFilterTest extends AbstractQosTest {
    @Test
    public void testReaderSideFiltering() {
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 10));
        qos.setPolicy(new QosDurability(Kind.TRANSIENT_LOCAL));
        
        ContentFilter<HelloMessage> cf = new ContentFilter<HelloMessage>() {
        	int count = 0;
        	@Override
			public boolean acceptSample(Sample<HelloMessage> sample) {
        		return count++ % 2 == 0; // Accept every other sample
			}

			@Override
			public ContentFilterProperty getContentFilterProperty() {
				return null;
			}
		};
        
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);
        dr.setContentFilter(cf);
        
        final CountDownLatch dataLatch = new CountDownLatch(4); // Expect to receive 4 samples
        dr.addSampleListener(new SampleListener<HelloMessage>() {
			@Override
			public void onSamples(List<Sample<HelloMessage>> samples) {
				for (Sample<HelloMessage> s : samples) {
					//System.out.println("Got " + s.getData());
					dataLatch.countDown();
				}
			}
		});
        
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);

        final CountDownLatch emLatch = new CountDownLatch(2); // Latch used to synchronize on entity matched
        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw, null, emLatch);
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);
        
        for (int i = 0; i < 8; i++) { // Write 10 message, filter should accept every other
        	dw.write(new HelloMessage(i, "hello"));
        }

        waitFor(dataLatch, LATCH_WAIT_MILLIS, true);
        assertEquals(4, dr.getSamples().size()); 
    }


    @Test
    public void testWriterSideFiltering() {
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 10));
        qos.setPolicy(new QosDurability(Kind.TRANSIENT_LOCAL));
        
        ContentFilter<HelloMessage> cf = new ContentFilter<HelloMessage>() {
        	int count = 0;
        	@Override
			public boolean acceptSample(Sample<HelloMessage> sample) {
        		return count++ % 2 == 0; // Accept every other sample
			}

			@Override
			public ContentFilterProperty getContentFilterProperty() {
				return new ContentFilterProperty("cfTopicName", "relatedTopicName", 
						"filterClassName", "filterExpression");
			}
		};
        
        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class, qos);
        dr.setContentFilter(cf);
        
        final CountDownLatch dataLatch = new CountDownLatch(2); // Expect to receive 2 samples
        dr.addSampleListener(new SampleListener<HelloMessage>() {
			@Override
			public void onSamples(List<Sample<HelloMessage>> samples) {
				for (Sample<HelloMessage> s : samples) {
					dataLatch.countDown();
				}
			}
		});
        
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class, qos);
        dw.registerContentFilter(cf); // Explicitly register writer side content filter 
        
        final CountDownLatch emLatch = new CountDownLatch(2); // Latch used to synchronize on entity matched
        addCommunicationListener(dr, null, emLatch);
        addCommunicationListener(dw, null, emLatch);
        waitFor(emLatch, LATCH_WAIT_MILLIS, true);
        
        for (int i = 0; i < 8; i++) { // Write 10 message, filter should accept every other
        	dw.write(new HelloMessage(i, "hello"));
        }
        
        waitFor(dataLatch, LATCH_WAIT_MILLIS, true);
        assertEquals(2, dr.getSamples().size()); 
    }
}
