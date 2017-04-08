package examples.rpc;

import java.io.IOException;

import net.sf.jrtps.rpc.ServiceManager;

public class MyServer implements SampleService {
   public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
      ServiceManager mgr = new ServiceManager();
      SampleService service = new MyServer(); 
      mgr.registerService(service);
      
      System.out.println("\n*** Press enter to close services ***\n");
      System.in.read();
   }
   
   @Override
   public int power2(int value) {
      System.out.println("power2(" + value + ") was called");
      
      return value * value;
   }
}
