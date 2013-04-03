package alt.rtps.builtin;

import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.Marshaller;

public class TopicDataMarshaller extends Marshaller<TopicData> {

	@Override
	public TopicData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		TopicData wd = new TopicData(plEnc.getParameterList());
		
		return wd;
	}

	@Override
	public DataEncapsulation marshall(TopicData td) {
		ParameterList payloadParams = new ParameterList();
		 
		payloadParams.add(new TopicName(td.getTopicName()));
		payloadParams.add(new TypeName(td.getTypeName()));
		payloadParams.add(new KeyHash(td.getKey().getBytes()));
		
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}

}
