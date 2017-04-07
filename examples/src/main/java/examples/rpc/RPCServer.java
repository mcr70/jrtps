package examples.rpc;

import java.io.IOException;

import net.sf.jrtps.rpc.ServiceManager;

public class RPCServer implements SampleService {
   public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
      ServiceManager mgr = new ServiceManager();
      RPCServer service = new RPCServer(); 
      mgr.registerService(service);
      
      System.out.println("\n*** Press enter to close services ***\n");
      System.in.read();
   }
   
   @Override
   public int power2(int value) {
      System.out.println("foo(" + value + ") was called");
      
      return value * value;
   }
}
