package net.sf.jrtps.rpc;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Serializer is used to serialize Java types to/from RTPSByteBuffer.
 * 
 * @author mcr70
 */
public interface Serializer {
   /**
    * Serialize an Object into RTPSByteBuffer.
    * @param value Object to serialize
    * @param bb RTPSByteBuffer where Object is serialized
    */
   void serialize(Object value, RTPSByteBuffer bb) throws SerializationException;
   Object deSerialize(Class<?> type, RTPSByteBuffer bb) throws SerializationException;
}
