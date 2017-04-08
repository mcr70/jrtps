package examples.rpc;

import net.sf.jrtps.rpc.ServiceManager;

public class MyClient {
   public static void main(String[] args) throws Exception {
      ServiceManager mgr = new ServiceManager();
      SampleService client = mgr.createClient(SampleService.class);
      
      for(int i = 0; i < 10; i++) {
         int result = client.power2(i);

         System.out.println(i + "^2 == " + result);
         Thread.sleep(1000);
      }
   }
}
