package net.sf.jrtps.builtin;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

public class WriterDataMarshaller extends Marshaller<WriterData> {
	@Override
	public WriterData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		WriterData wd = new WriterData(plEnc.getParameterList());
		
		return wd;
	}

	@Override
	public DataEncapsulation marshall(WriterData wd) {
		ParameterList payloadParams = new ParameterList();

		payloadParams.add(new TopicName(wd.getTopicName()));
		payloadParams.add(new TypeName(wd.getTypeName()));
		payloadParams.add(new KeyHash(wd.getKey().getBytes()));
		
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}
}
