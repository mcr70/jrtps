package net.sf.jrtps.rpc;

/**
 * SerializationException is used to indicate a failure in serialization.
 * @author mcr70
 */
public class SerializationException extends Exception {
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new SerializationException
    * @param message Message
    */
   public SerializationException(String message) {
      super(message);
   }
}
