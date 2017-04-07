package examples.rpc;

import net.sf.jrtps.rpc.ServiceManager;

public class RPCClient {
   public static void main(String[] args) throws Exception {
      ServiceManager mgr = new ServiceManager();
      SampleService client = mgr.createClient(SampleService.class);
      
      Thread.sleep(1000);
      for(int i = 0; i < 10; i++) {
         long l1 = System.currentTimeMillis();
         int result = client.power2(i);
         long l2 = System.currentTimeMillis();
         System.out.println(i + "^2 == " + result + ", in " + (l2-l1) + " ms");
         Thread.sleep(1000);
      }
   }
}
