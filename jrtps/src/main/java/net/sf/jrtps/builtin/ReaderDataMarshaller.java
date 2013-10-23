package net.sf.jrtps.builtin;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

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