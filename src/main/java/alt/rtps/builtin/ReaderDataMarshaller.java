package alt.rtps.builtin;

import alt.rtps.Marshaller;
import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;

public class ReaderDataMarshaller extends Marshaller<ReaderData> {

	@Override
	public ReaderData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		ReaderData rd = new ReaderData(plEnc.getParameterList());
		
		return rd;
	}

	@Override
	public DataEncapsulation marshall(ReaderData rd) {
		ParameterList payloadParams = new ParameterList();
		 
		payloadParams.add(new TopicName(rd.getTopicName()));
		payloadParams.add(new TypeName(rd.getTypeName()));
		payloadParams.add(new KeyHash(rd.getKey().getBytes()));
		
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}
}
