package net.sf.jrtps;

import java.io.IOException;

import net.sf.jrtps.message.data.DataEncapsulation;


/**
 * Marshaller is used to transform Object to/from different data encodings.  
 * 
 * @author mcr70
 *
 * @param <T> Type of this Marshaller. Type is used to enforce symmetry
 * 			  between unmarshall and marshall methods.
 */
public interface Marshaller<T> {
	/**
	 * Determines whether or not a key is associated with type T.
	 */
	public boolean hasKey();

	/**
	 * Extracts a key from given object. If null is returned, it is assumed to be the same as 
	 * a byte array of length 0. 
	 * 
	 * @param data
	 * @return key
	 */
	public byte[] extractKey(T data);

	/**
	 * Unmarshalls given DataEncapsulation to Object.
	 * @param dEnc
	 * @return An instance of type T
	 * @throws IOException 
	 */
	public T unmarshall(DataEncapsulation dEnc) throws IOException;

	/**
	 * Marshalls given Object to DataEncapsulation
	 * @param data 
	 * @return DataEncapsulation
	 * @throws IOException 
	 */
	public DataEncapsulation marshall(T data) throws IOException;
}
