package alt.rtps.builtin;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.message.Data;
import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;

public class WriterDataMarshaller extends Marshaller<WriterData> {

	@Override
	public WriterData unmarshall(RTPSByteBuffer bb) {
		WriterData wd = new WriterData(bb);
		
		return wd;
	}

	@Override
	public Data marshall(WriterData wd) {
		List<Parameter> inlineQosParams = new LinkedList<Parameter>();
		inlineQosParams.add(new Sentinel());
		
		List<Parameter> payloadParams = new LinkedList<Parameter>(); 
		payloadParams.add(new TopicName(wd.getTopicName()));
		payloadParams.add(new TypeName(wd.getTypeName()));
		payloadParams.add(new KeyHash(wd.getKey().getBytes()));
		
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());
		//payloadParams.add(new VendorId(wd.getVendorId()));

//		Data data = new Data(EntityId_t.UNKNOWN_ENTITY, EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER, 
//				1, pd.getGuid(), 0x03cf, inlineQosParams, payloadParams);

		return null;
	}

}
