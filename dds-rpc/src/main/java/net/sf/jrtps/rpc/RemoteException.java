package net.sf.jrtps.rpc;

/**
 * RemoteException.  
 * 
 * @author mcr70
 */
public class RemoteException extends Exception {
   private static final long serialVersionUID = 1L;

   public RemoteException(String string) {
      super(string);
   }
}
