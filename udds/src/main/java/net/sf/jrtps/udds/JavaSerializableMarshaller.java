package net.sf.jrtps.udds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default Marshaller used by udds.
 * This Marshaller marshalls objects by writing them to <i>ObjectOutputStream</i> and reading from
 * <i>ObjectInputStream</i>. JavaSerializableMarshaller supports net.sf.jrtps.Key annotation
 * to form a Key for the marshalled Object.
 * 
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see net.sf.jrtps.Key
 * 
 * @author mcr70
 *
 */
public class JavaSerializableMarshaller extends Marshaller<Serializable> {
	private static final Logger log = LoggerFactory.getLogger(JavaSerializableMarshaller.class);
	private int bufferSize;

	/**
	 * Constructs this JavaSerializableMarshaller bufferSize 1024. This constructor is used 
	 * by udds.
	 */
	public JavaSerializableMarshaller() {
		this(1024);
	}
	
	/**
	 * Constructs this JavaSerializableMarshaller with given bufferSize.
	 * bufferSize must be big enough to hold serialized object. 
	 * @param bufferSize the size of the buffer that is used during marshall and unmarshall
	 */
	public JavaSerializableMarshaller(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Extracts key from given data. Object is searched for fields with annotation @Key.
	 * These fields form a key of the data.
	 * 
	 * @param data
	 * @return a key hash of the annotated field values.
	 */
	@Override
	public byte[] extractKey(Serializable data) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
			ObjectOutputStream oos = new ObjectOutputStream(baos);

			for (Field f : keyFields) {
				f.setAccessible(true);
				Object value = f.get(data);
				oos.writeObject(value);
			}
			
			return baos.toByteArray();
		}
		catch(Exception e) {
			log.warn("Failed to extract key from {}", data, e);
		}
		
		return null;
	}

	/**
	 * Unmarshalls an object from DataEncapsulation.
	 * @param dEnc
	 * @return Serializable
	 */
	@Override
	public Serializable unmarshall(DataEncapsulation dEnc) throws IOException {
		CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;		
		RTPSByteBuffer bb = cdrEnc.getBuffer();

		ObjectInputStream ois = new ObjectInputStream(bb.getInputStream());

		Object o = null;
		try {
			o = ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return (Serializable) o;
	}

	/**
	 * Marshalls a given Serializable Object into DataEncapsulation.
	 * 
	 * @param data Data to marshall
	 * @return DataEncapsulation
	 */
	@Override
	public DataEncapsulation marshall(Serializable data) throws IOException {
		CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
		RTPSByteBuffer bb = cdrEnc.getBuffer();

		ObjectOutputStream os = new ObjectOutputStream(bb.getOutputStream());
		os.writeObject(data);

		return cdrEnc;
	}
}
