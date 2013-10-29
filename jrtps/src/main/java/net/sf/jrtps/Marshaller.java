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
public abstract class Marshaller<T> {
	/**
	 * Unmarshalls given DataEncapsulation to Object.
	 * @param dEnc
	 * @return An Object of type T
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
