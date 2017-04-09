package net.sf.jrtps.udds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Marshaller marshalls objects by writing them to
 * <i>ObjectOutputStream</i> and reading from <i>ObjectInputStream</i>.
 * SerializableMarshaller supports net.sf.jrtps.udds.Key annotation to form
 * a Key for the marshalled Object.
 * 
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see net.sf.jrtps.udds.Key
 * 
 * @author mcr70
 */
class SerializableMarshaller implements Marshaller<Serializable> {
    private static final Logger log = LoggerFactory.getLogger(SerializableMarshaller.class);

    private final int bufferSize;
    private final Field[] keyFields;
    private final boolean hasKey;

    /**
     * Constructs this JavaSerializableMarshaller bufferSize 1024. This
     * constructor is used by udds.
     * 
     * @param type
     */
    public SerializableMarshaller(Class<?> type) {
        this(type, 1024);
    }

    /**
     * Constructs this JavaSerializableMarshaller with given bufferSize.
     * bufferSize must be big enough to hold serialized object.
     * 
     * @param type
     * @param bufferSize
     *            the size of the buffer that is used during marshall and
     *            unmarshall
     */
    public SerializableMarshaller(Class<?> type, int bufferSize) {
        this.bufferSize = bufferSize;
        this.keyFields = getKeyFields(type);
        this.hasKey = keyFields.length > 0;
    }

    @Override
    public boolean hasKey() {
        return hasKey;
    }

    /**
     * Extracts key from given data. Object is searched for fields with
     * annotation @Key. These fields form a key of the data.
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
        } catch (Exception e) {
            log.warn("Failed to extract key from {}", data, e);
        }

        return null;
    }

    /**
     * Unmarshalls an object from DataEncapsulation.
     * 
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
        finally {
            ois.close();
        }
        
        return (Serializable) o;
    }

    /**
     * Marshalls a given Serializable Object into DataEncapsulation.
     * 
     * @param data
     *            Data to marshall
     * @return DataEncapsulation
     */
    @Override
    public DataEncapsulation marshall(Serializable data) throws IOException {
        CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
        RTPSByteBuffer bb = cdrEnc.getBuffer();

        ObjectOutputStream os = new ObjectOutputStream(bb.getOutputStream());
        try {
            os.writeObject(data);
        }
        finally{
            os.close();
        }

        return cdrEnc;
    }

    /**
     * Get all the keyFields found from given type. If no key fields are found,
     * an array of length 0 is returned.
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
