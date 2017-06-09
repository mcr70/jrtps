package net.sf.jrtps.rpc;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Serializer is used to serialize argument and return types to/from RTPSByteBuffer.
 */
public interface Serializer<T> {
   /**
    * Serialize an Object into RTPSByteBuffer.
    * @param value Object to serialize
    * @param bb RTPSByteBuffer where Object is serialized
    * @throws SerializationException When value cannot be serialized
    */
   void serialize(T value, RTPSByteBuffer bb) throws SerializationException;
   
   /**
    * DeSerializes an Object of given type from RTPSByteBuffer.
    * @param type Type of the Object expected to be found from byte buffer
    * @param bb byte buffer
    * @return a deserialized Object
    * @throws SerializationException If type cannot be deserialized to Object
    */
   T deSerialize(Class<T> type, RTPSByteBuffer bb) throws SerializationException;
}
