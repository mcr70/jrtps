package net.sf.jrtps.udds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JavaSerializableMarshaller extends Marshaller<Object> {
	private static final Logger log = LoggerFactory.getLogger(JavaSerializableMarshaller.class);

	@Override
	public byte[] extractKey(Object data) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			RTPSByteBuffer bb = new RTPSByteBuffer(new byte[1024]);
			for (Field f : keyFields) {
				f.setAccessible(true);
				Object value = f.get(data);
				System.out.println("*** Writing key: " + value);
				oos.writeObject(value);
			}
			
			return baos.toByteArray();
		}
		catch(Exception e) {
			log.warn("Failed to extract key from {}", data, e);
		}
		
		return null;
	}

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
		CDREncapsulation cdrEnc = new CDREncapsulation(1024); // TODO: hardcoded
		RTPSByteBuffer bb = cdrEnc.getBuffer();

		ObjectOutputStream os = new ObjectOutputStream(bb.getOutputStream());
		os.writeObject(data);

		return cdrEnc;
	}
}
