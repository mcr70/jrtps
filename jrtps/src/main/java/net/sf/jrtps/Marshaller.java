package net.sf.jrtps;

import java.io.IOException;

import net.sf.jrtps.message.DataEncapsulation;

/**
 * Marshaller is used to transform Object to/from different data encodings.
 * 
 * @param <T> Type of this Marshaller. Type is used to enforce symmetry between
 *            unmarshall and marshall methods.
 * @author mcr70
 */
public interface Marshaller<T> {
    /**
     * Determines whether or not a key is associated with type T.
     * 
     * @return true if a key is associated with type T
     */
    public boolean hasKey();

    /**
     * Extracts a key from given object. If null is returned, it is assumed to
     * be the same as a byte array of length 0. Returned byte array can be of any length.
     * However, if the byte arrays length is greater than 15, it is internally converted to
     * a MD5 hash. 
     * 
     * @param data Object that holds key
     * @return key
     */
    public byte[] extractKey(T data);

    /**
     * Unmarshalls given DataEncapsulation to Object.
     * 
     * @param dEnc DataEncapsulation
     * @return An instance of type T
     * @throws IOException if a failure is detected during operation
     */
    public T unmarshall(DataEncapsulation dEnc) throws IOException;

    /**
     * Marshalls given Object to DataEncapsulation
     * 
     * @param data An Object to marshall
     * @return DataEncapsulation
     * @throws IOException if a failure is detected during operation
     */
    public DataEncapsulation marshall(T data) throws IOException;
}
