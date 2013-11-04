package net.sf.jrtps;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;

import net.sf.jrtps.message.data.DataEncapsulation;


/**
 * Marshaller is used to transform Object to/from different data encodings.  
 * 
 * @author mcr70
 *
 * @param <T> Type of this Marshaller. Type is used to enforce symmetry
 * 			  between unmarshall and marshall methods.
 */
public abstract class Marshaller<T> {
	protected Field[] keyFields;

	/**
	 * Determines whether or not a key is associated with type T.
	 * Classes fields are examined for annotation Key. If one is found,
	 * true is returned.
	 *  
	 * @return true, if type T has a key
	 */
	public boolean hasKey(Class<?> type) {
		if (keyFields == null) {
			Field[] fields = type.getDeclaredFields();
			LinkedList<Field> fieldList = new LinkedList<>();
			for (Field f : fields) {
				Key key = f.getAnnotation(Key.class);
				if (key != null) {
					fieldList.add(f);
				}
			}

			keyFields = fieldList.toArray(new Field[0]);
		}

		return keyFields.length > 0;
	}

	/**
	 * Extracts a key from given object. If null is returned, it is assumed to be the same as 
	 * a byte array of length 0. 
	 * 
	 * @param data
	 * @return key
	 */
	public abstract byte[] extractKey(T data);

	/**
	 * Unmarshalls given DataEncapsulation to Object.
	 * @param dEnc
	 * @return An instance of type T
	 * @throws IOException 
	 */
	public abstract T unmarshall(DataEncapsulation dEnc) throws IOException;

	/**
	 * Marshalls given Object to DataEncapsulation
	 * @param data 
	 * @return DataEncapsulation
	 * @throws IOException 
	 */
	public abstract DataEncapsulation marshall(T data) throws IOException;
}
