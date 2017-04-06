package net.sf.jrtps.rpc;

/**
 * NoSuchSerializer exception. 
 * @author mcr70
 */
public class NoSuchSerializer extends Exception {
   private static final long serialVersionUID = 1L;

   /**
    * NoSuchSerializer
    * @param type Type
    */
   public NoSuchSerializer(Class<?> type) {
      super("No such Serializer: " + type.getName());
   }
}
