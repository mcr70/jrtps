package net.sf.jrtps.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.transport.RTPSByteBuffer;

class JavaPrimitiveSerializer implements Serializer {
   private static final Logger logger = LoggerFactory.getLogger(JavaPrimitiveSerializer.class);
   
   public void serialize(Object value, RTPSByteBuffer bb) throws SerializationException {
      Class<?> type = value.getClass();
      
      if (type.equals(int.class) || type.equals(Integer.class)) { // Int32
         bb.writeInt((Integer)value);
      }      
      else if (type.equals(short.class)) { // Int16
         bb.writeShort((Short)value);
      }
      else if (type.equals(long.class)) { // Int64
         bb.writeLong((Long)value);
      }
      else if (type.equals(float.class)) { // Float32
         bb.writeFloat((Float)value);                
      }
      else if (type.equals(double.class)) { // Float64
         bb.writeDouble((Double)value);                
      }
      else if (type.equals(char.class)) { // Char8
         bb.writeChar((Character)value);                
      }
      else if (type.equals(byte.class)) { // Byte
         bb.writeByte((Byte)value);                
      }
      else if (type.equals(boolean.class)) { // Boolean
         bb.writeBoolean((Boolean)value);                
      }
      else if (type.equals(String.class)) { // string
         bb.writeString((String)value);
      }
      
      throw new SerializationException("Failed to serialize " + type.getName());
   }
   
   public Object deSerialize(Class<?> type, RTPSByteBuffer bb) throws SerializationException {
      if (type.equals(int.class) || type.equals(Integer.class)) { // Int32
         return bb.readInt();
      }
      else if (type.equals(short.class)) { // Int16
         return bb.readShort();
      }
      else if (type.equals(long.class)) { // Int64
         return bb.readLong();
      }
      else if (type.equals(float.class)) { // Float32
         return bb.readFloat();                
      }
      else if (type.equals(double.class)) { // Float64
         return bb.readDouble();                
      }
      else if (type.equals(char.class)) { // Char8
         return bb.readChar();                
      }
      else if (type.equals(byte.class)) { // Byte
         return bb.readByte();                
      }
      else if (type.equals(boolean.class)) { // Boolean
         return bb.readBoolean();                
      }
      else if (type.equals(String.class)) { // string
         return bb.readString();
      }
      
      throw new SerializationException("Failed to deSerialize " + type.getName());
   }
}
