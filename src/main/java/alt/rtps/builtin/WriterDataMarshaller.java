package alt.rtps.builtin;

import alt.rtps.Marshaller;
import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;

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
