package net.sf.jrtps.udds;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.CDREncapsulation;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Marshaller marshalls objects by writing them to <i>ObjectOutput</i> and reading from
 * <i>ObjectInput</i>. JavaExternalizableMarshaller supports net.sf.jrtps.udds.Key annotation
 * to form a Key for the marshalled Object.
 * 
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see net.sf.jrtps.udds.Key
 * 
 * @author mcr70
 */
public class JavaExternalizableMarshaller implements Marshaller<Externalizable> {
	private static final Logger log = LoggerFactory.getLogger(JavaExternalizableMarshaller.class);

	private final int bufferSize;
	private final Field[] keyFields;
	private final boolean hasKey;

	private final Class<? extends Externalizable> type;
	
	/**
	 * Constructs this JavaExternalizableMarshaller bufferSize 1024. This constructor is used 
	 * by udds.
	 * @param type 
	 */
	public JavaExternalizableMarshaller(Class<? extends Externalizable> type) {
		this(type, 1024);
	}

	/**
	 * Constructs this JavaExternalizableMarshaller with given bufferSize.
	 * bufferSize must be big enough to hold serialized object. 
	 * @param type 
	 * @param bufferSize the size of the buffer that is used during marshall and unmarshall
	 */
	public JavaExternalizableMarshaller(Class<? extends Externalizable> type, int bufferSize) {
		this.type = type;
		this.bufferSize = bufferSize;
		this.keyFields = getKeyFields(type);
		this.hasKey = keyFields.length > 0;
	}


	@Override
	public boolean hasKey() {
		return hasKey;
	}

	/**
	 * Extracts key from given data. Object is searched for fields with annotation @Key.
	 * These fields form a key of the data.
	 * 
	 * @param data
	 * @return a key hash of the annotated field values.
	 */
	@Override
	public byte[] extractKey(Externalizable data) {
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
	 * @return Externalizable
	 */
	@Override
	public Externalizable unmarshall(DataEncapsulation dEnc) throws IOException {
		CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;		
		RTPSByteBuffer bb = cdrEnc.getBuffer();


		Externalizable obj = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bb.getInputStream());
			obj = type.newInstance();
			obj.readExternal(ois);
		} catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			if (ois != null) {
				ois.close();
			}
		}
		
		return obj;
	}

	/**
	 * Marshalls a given Externalizable Object into DataEncapsulation.
	 * 
	 * @param data Data to marshall
	 * @return DataEncapsulation
	 */
	@Override
	public DataEncapsulation marshall(Externalizable data) throws IOException {
		CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
		RTPSByteBuffer bb = cdrEnc.getBuffer();
		
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bb.getOutputStream());
			data.writeExternal(oos);
		}
		finally {
			if (oos != null) {
				oos.close();
			}
		}
		
		return cdrEnc;
	}

	/**
	 * Get all the keyFields found from given type. If no key fields are found, an array of length 0 is returned.
	 * 
	 * @return an array of key fields
	 */
	private Field[] getKeyFields(final Class<?> type) {			
		Field[] fields = type.getDeclaredFields();
		LinkedList<Field> fieldList = new LinkedList<>();
		for (Field f : fields) {
			Key key = f.getAnnotation(Key.class);
			if (key != null) {
				fieldList.add(f);
			}
		}

		// Sort fields according to @Key annotations index field
		Collections.sort(fieldList, new Comparator<Field>() {
			@Override
			public int compare(Field f1, Field f2) {
				Key key1 = f1.getAnnotation(Key.class);
				Key key2 = f2.getAnnotation(Key.class);

				if (key1.index() == key2.index()) {
					throw new RuntimeException(type + " has two Key annotations with same index: " + key1.index());
				}

				return key1.index() - key2.index();
			}
		});

		Field[] keyFields = fieldList.toArray(new Field[0]);

		return keyFields;
	}

}
