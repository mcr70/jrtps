package examples.rpc;

import java.io.IOException;

import net.sf.jrtps.rpc.ServiceManager;

public class RPCServer implements SampleService {
   public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
      ServiceManager mgr = new ServiceManager();
      RPCServer service = new RPCServer(); 
      mgr.registerService(service);
   }
   
   @Override
   public void foo(int value) {
      System.out.println("foo(" + value + ") was called");
   }
}
