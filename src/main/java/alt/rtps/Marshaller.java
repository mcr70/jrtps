package alt.rtps;

import java.io.IOException;

import alt.rtps.message.data.DataEncapsulation;

/**
 * Marshaller is used to transform Object to/from different data encodings.  
 * 
 * @author mcr70
 *
 * @param <T> Type of this Marshaller
 */
public abstract class Marshaller<T> {
	/**
	 * Unmarshalls given DataEncapsulation to Object.
	 * @param dEnc
	 * @return
	 * @throws IOException 
	 */
	public abstract T unmarshall(DataEncapsulation dEnc) throws IOException;

	/**
	 * Marshalls given Object to DataEncapsulation
	 * @param data 
	 * @return
	 * @throws IOException 
	 */
	public abstract DataEncapsulation marshall(T data) throws IOException;
}
