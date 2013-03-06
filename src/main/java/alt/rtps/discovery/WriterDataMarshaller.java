package alt.rtps.discovery;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.message.Data;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;

public class WriterDataMarshaller extends Marshaller<WriterData> {

	@Override
	public WriterData unmarshall(RTPSByteBuffer bb) {
		WriterData wd = new WriterData(null, bb);
		
		return wd;
	}

	@Override
	public Data marshall(WriterData wd) {
		// TODO: ...
		
		List<Parameter> payloadParams = new LinkedList<Parameter>(); 
		payloadParams.add(new TopicName(wd.getTopicName()));
		payloadParams.add(new TypeName(wd.getTypeName()));
		
		// payloadParams.add wd.getKey();
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());
		//payloadParams.add(new VendorId(wd.getVendorId()));

		
		//Data d = new D
		return null;
	}

}
