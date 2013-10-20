package net.sf.jrtps.builtin;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

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
