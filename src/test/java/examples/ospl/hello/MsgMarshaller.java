package examples.ospl.hello;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class MsgMarshaller implements Marshaller<Msg> {

    @Override
    public boolean hasKey() {
        return true;
    }

    @Override
    public byte[] extractKey(Msg data) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) data.userID;
        return bytes;
    }

    @Override
    public Msg unmarshall(DataEncapsulation dEnc) throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        Msg msg = new Msg();
        msg.userID = bb.read_long();
        msg.message = bb.read_string();

        return msg;
    }

    @Override
    public DataEncapsulation marshall(Msg msg) throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(1024);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        bb.write_long(msg.userID);
        bb.write_string(msg.message);

        return cdrEnc;
    }

}
