package alt.rtps.builtin;

import alt.rtps.message.Data;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;

public class TopicDataMarshaller extends Marshaller<TopicData> {

	@Override
	public TopicData unmarshall(Data data) {
		RTPSByteBuffer bb = data.getSerializedPayloadInputStream();
		TopicData wd = new TopicData(null, bb);
		
		return wd;
	}

	@Override
	public Data marshall(TopicData td) {
		System.out.println("WriterDataMarshaller.toData() NOT implemented");
		return null;
	}

}
