package alt.rtps.builtin;

import alt.rtps.message.Data;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.transport.Marshaller;

public class ReaderDataMarshaller extends Marshaller<ReaderData> {

	@Override
	public ReaderData unmarshall(Data data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data.getDataEncapsulation();
		ReaderData rd = new ReaderData(plEnc.getParameterList());
		
		return rd;
	}

	@Override
	public Data marshall(ReaderData data) {
		System.out.println("ReaderDaraMarshaller.toData() NOT implemented");
		return null;
	}

}
