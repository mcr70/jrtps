package net.sf.udds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

class JavaSerializableMarshaller extends Marshaller {

	@Override
	public Object unmarshall(DataEncapsulation dEnc) throws IOException {
		CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;		
		RTPSByteBuffer bb = cdrEnc.getBuffer();

		ObjectInputStream ois = new ObjectInputStream(bb.getInputStream());
		
		Object o = null;
		try {
			o = ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return o;
	}

	@Override
	public DataEncapsulation marshall(Object data) throws IOException {
		CDREncapsulation cdrEnc = new CDREncapsulation(1024);
		RTPSByteBuffer bb = cdrEnc.getBuffer();
		
		ObjectOutputStream os = new ObjectOutputStream(bb.getOutputStream());
		os.writeObject(data);
		
		return cdrEnc;
	}
}
