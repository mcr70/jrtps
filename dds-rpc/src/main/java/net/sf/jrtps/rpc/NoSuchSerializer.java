package net.sf.jrtps.rpc;

public class NoSuchSerializer extends Exception {
   private static final long serialVersionUID = 1L;

   public NoSuchSerializer(Class<?> returnType) {
      super("No such Serializer: " + returnType.getName());
   }
}
