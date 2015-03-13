package net.sf.jrtps.aperf1;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class HelloMarshaller implements Marshaller<Hello> {

    @Override
    public boolean hasKey() {
        return false;
    }

    @Override
    public byte[] extractKey(Hello data) {
        return new byte[0];
    }

    @Override
    public Hello unmarshall(DataEncapsulation dEnc) throws IOException {
        CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        Hello msg = new Hello();
        msg.message = bb.read_string();
        
        return msg;
    }

    @Override
    public DataEncapsulation marshall(Hello msg) throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(1024);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        bb.write_string(msg.message);

        return cdrEnc;
    }

}
