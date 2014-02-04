package examples.opendds.messenger;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class MessageMarshaller implements Marshaller<Message> {

	@Override
	public boolean hasKey() {
		return true;
	}

	@Override
	public byte[] extractKey(Message data) {
		byte[] bytes = new byte[1];
		bytes[0] = (byte) data.subject_id;
		return bytes;
	}

	@Override
	public Message unmarshall(DataEncapsulation dEnc) throws IOException {
		CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;		
		RTPSByteBuffer bb = cdrEnc.getBuffer();

		Message msg = new Message();
		msg.from = bb.read_string();
		msg.subject = bb.read_string();
		msg.subject_id = bb.read_long();
		msg.text = bb.read_string();
		msg.count = bb.read_long();

		return msg;
	}

	@Override
	public DataEncapsulation marshall(Message msg) throws IOException {
		CDREncapsulation cdrEnc = new CDREncapsulation(1024);
		RTPSByteBuffer bb = cdrEnc.getBuffer();
		
		bb.write_string(msg.from);
		bb.write_string(msg.subject);
		bb.write_long(msg.subject_id);
		bb.write_string(msg.text);
		bb.write_long(msg.count);
		
		return cdrEnc;
	}

}
