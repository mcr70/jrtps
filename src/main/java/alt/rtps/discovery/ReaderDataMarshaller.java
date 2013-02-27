package alt.rtps.discovery;

import alt.rtps.message.Data;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;

public class ReaderDataMarshaller extends Marshaller {

	@Override
	public Object unmarshall(RTPSByteBuffer bb) {
		ReaderData rd = new ReaderData(bb);
		
		return rd;
	}

	@Override
	public Data marshall(Object data) {
		System.out.println("ReaderDaraMarshaller.toData() NOT implemented");
		return null;
	}

}
