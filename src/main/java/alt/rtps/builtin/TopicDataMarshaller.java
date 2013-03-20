package alt.rtps.builtin;

import alt.rtps.message.Data;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.transport.Marshaller;

public class TopicDataMarshaller extends Marshaller<TopicData> {

	@Override
	public TopicData unmarshall(Data data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data.getDataEncapsulation();
		TopicData wd = new TopicData(plEnc.getParameterList());
		
		return wd;
	}

	@Override
	public Data marshall(TopicData td) {
		System.out.println("WriterDataMarshaller.toData() NOT implemented");
		return null;
	}

}
