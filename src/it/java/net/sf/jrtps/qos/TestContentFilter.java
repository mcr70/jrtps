package net.sf.jrtps.qos;

import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.ContentFilter;
import examples.hello.serializable.HelloMessage;

public class TestContentFilter implements ContentFilter<HelloMessage>{
	int count = 0;
	@Override
	public boolean acceptSample(Sample<HelloMessage> sample) {
		return count++ % 2 == 0; // Accept every other sample
	}

	@Override
	public ContentFilterProperty getContentFilterProperty() {
		return new ContentFilterProperty("cfTopicName", "relatedTopicName", 
				TestContentFilter.class);
	}
}
