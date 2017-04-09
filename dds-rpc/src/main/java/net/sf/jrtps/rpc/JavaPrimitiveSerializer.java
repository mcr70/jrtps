package net.sf.jrtps.rpc;

import net.sf.jrtps.transport.RTPSByteBuffer;

class JavaPrimitiveSerializer implements Serializer {
   
   public void serialize(Object o, RTPSByteBuffer bb) throws SerializationException {
      Class<?> type = o.getClass();
      
      if (type.equals(int.class) || type.equals(Integer.class)) { // Int32
         bb.writeInt((Integer)o);
      }      
      else if (type.equals(short.class) || type.equals(Short.class)) { // Int16
         bb.writeShort((Short)o);
      }
      else if (type.equals(long.class) || type.equals(Long.class)) { // Int64
         bb.writeLong((Long)o);
      }
      else if (type.equals(float.class) || type.equals(Float.class)) { // Float32
         bb.writeFloat((Float)o);                
      }
      else if (type.equals(double.class) || type.equals(Double.class)) { // Float64
         bb.writeDouble((Double)o);                
      }
      else if (type.equals(char.class) || type.equals(Character.class)) { // Char8
         bb.writeChar((Character)o);                
      }
      else if (type.equals(byte.class) || type.equals(Byte.class)) { // Byte
         bb.writeByte((Byte)o);                
      }
      else if (type.equals(boolean.class) || type.equals(Boolean.class)) { // Boolean
         bb.writeBoolean((Boolean)o);                
      }
      else if (type.equals(String.class)) { // string
         bb.writeString((String)o);
      }
      else if (type.equals(int[].class)) {
         int[] array = (int[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Integer[].class)) {
         Integer[] array = (Integer[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(short[].class)) {
         short[] array = (short[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Short[].class)) {
         Short[] array = (Short[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(long[].class)) {
         long[] array = (long[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Long[].class)) {
         Long[] array = (Long[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(float[].class)) {
         float[] array = (float[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Float[].class)) {
         Float[] array = (Float[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(double[].class)) {
         double[] array = (double[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Double[].class)) {
         Double[] array = (Double[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(char[].class)) {
         char[] array = (char[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Character[].class)) {
         Character[] array = (Character[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(byte[].class)) {
         byte[] array = (byte[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Byte[].class)) {
         Byte[] array = (Byte[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(boolean[].class)) {
         boolean[] array = (boolean[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(Boolean[].class)) {
         Boolean[] array = (Boolean[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else if (type.equals(String[].class)) {
         String[] array = (String[]) o;
         bb.write_long(array.length);
         for (int i = 0; i < array.length; i++) {
            serialize(array[i], bb);
         }
      }
      else {
         throw new SerializationException("Failed to serialize " + type.getName());
      }
   }
   
   public Object deSerialize(Class type, RTPSByteBuffer bb) throws SerializationException {
      if (type.equals(int.class) || type.equals(Integer.class)) { // Int32
         return bb.readInt();
      }
      else if (type.equals(short.class) || type.equals(Short.class)) { // Int16
         return bb.readShort();
      }
      else if (type.equals(long.class) || type.equals(Long.class)) { // Int64
         return bb.readLong();
      }
      else if (type.equals(float.class) || type.equals(Float.class)) { // Float32
         return bb.readFloat();                
      }
      else if (type.equals(double.class) || type.equals(Double.class)) { // Float64
         return bb.readDouble();                
      }
      else if (type.equals(char.class) || type.equals(Character.class)) { // Char8
         return bb.readChar();                
      }
      else if (type.equals(byte.class) || type.equals(Byte.class)) { // Byte
         return bb.readByte();                
      }
      else if (type.equals(boolean.class) || type.equals(Boolean.class)) { // Boolean
         return bb.readBoolean();                
      }
      else if (type.equals(String.class)) { // string
         return bb.readString();
      }
      else if (type.equals(int[].class)) { 
         int length = bb.read_long();
         int[] array = new int[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_long();
         }
         return array;
      }
      else if (type.equals(Integer[].class)) { 
         int length = bb.read_long();
         Integer[] array = new Integer[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_long();
         }
         return array;
      }
      else if (type.equals(short[].class)) { 
         int length = bb.read_long();
         short[] array = new short[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readShort();
         }
         
         return array;
      }
      else if (type.equals(Short[].class)) { 
         int length = bb.read_long();
         Short[] array = new Short[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readShort();
         }
         return array;
      }
      else if (type.equals(long[].class)) { 
         int length = bb.read_long();
         long[] array = new long[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_longlong();
         }
         return array;
      }
      else if (type.equals(Long[].class)) { 
         int length = bb.read_long();
         Long[] array = new Long[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_longlong();
         }
         return array;
      }
      else if (type.equals(float[].class)) { 
         int length = bb.read_long();
         float[] array = new float[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readFloat();
         }
         return array;
      }
      else if (type.equals(Float[].class)) { 
         int length = bb.read_long();
         Float[] array = new Float[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readFloat();
         }
         return array;
      }
      else if (type.equals(double[].class)) { 
         int length = bb.read_long();
         double[] array = new double[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readDouble();
         }
         return array;
      }
      else if (type.equals(Double[].class)) { 
         int length = bb.read_long();
         Double[] array = new Double[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readDouble();
         }
         return array;
      }
      else if (type.equals(char[].class)) { 
         int length = bb.read_long();
         char[] array = new char[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readChar();
         }         
         return array;
      }
      else if (type.equals(Character[].class)) { 
         int length = bb.read_long();
         Character[] array = new Character[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.readChar();
         }         
         return array;
      }
      else if (type.equals(byte[].class)) { 
         int length = bb.read_long();
         byte[] array = new byte[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_octet();
         }         
         return array;
      }
      else if (type.equals(Byte[].class)) { 
         int length = bb.read_long();
         Byte[] array = new Byte[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_octet();
         }         
         return array;
      }
      else if (type.equals(boolean[].class)) { 
         int length = bb.read_long();
         boolean[] array = new boolean[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_boolean();
         }
         return array;
      }
      else if (type.equals(Boolean[].class)) { 
         int length = bb.read_long();
         Boolean[] array = new Boolean[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_boolean();
         }         
         return array;
      }
      else if (type.equals(String[].class)) { 
         int length = bb.read_long();
         String[] array = new String[length];
         for (int i = 0; i < length; i++) {
            array[i] = bb.read_string();
         }
         return array;
      }
      
      throw new SerializationException("Failed to deSerialize " + type.getName());
   }
}
