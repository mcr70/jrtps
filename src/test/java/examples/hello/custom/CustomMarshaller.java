package examples.hello.custom;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class CustomMarshaller implements Marshaller<CustomHelloMessage> {

    @Override
    public boolean hasKey() {
        return true; // userID is our key
    }

    @Override
    public byte[] extractKey(CustomHelloMessage data) {
    	return ByteBuffer.allocate(4).putInt(data.userID).array();
    }

    @Override
    public CustomHelloMessage unmarshall(DataEncapsulation dEnc) throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        CustomHelloMessage msg = new CustomHelloMessage();
        msg.userID = bb.read_long();
        msg.message = bb.read_string();

        return msg;
    }

    @Override
    public DataEncapsulation marshall(CustomHelloMessage msg) throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(1024);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        bb.write_long(msg.userID);
        bb.write_string(msg.message);

        return cdrEnc;
    }
}
