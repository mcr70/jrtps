package alt.rtps.transport;

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
	 */
	public abstract T unmarshall(DataEncapsulation dEnc);

	/**
	 * Marshalls given Object to DataEncapsulation
	 * @param data 
	 * @return
	 */
	public abstract DataEncapsulation marshall(T data);
}
