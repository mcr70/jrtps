package alt.rtps.discovery;

import alt.rtps.message.Data;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;

public class TopicDataMarshaller extends Marshaller {

	@Override
	public Object unmarshall(RTPSByteBuffer bb) {
		TopicData wd = new TopicData(null, bb);
		
		return wd;
	}

	@Override
	public Data marshall(Object data) {
		System.out.println("WriterDataMarshaller.toData() NOT implemented");
		return null;
	}

}
