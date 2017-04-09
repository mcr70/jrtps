package examples.rpc;

import net.sf.jrtps.rpc.ServiceManager;

public class MyClient {
   public static void main(String[] args) throws Exception {
      ServiceManager mgr = new ServiceManager();
      SampleService client = mgr.createClient(SampleService.class);
      
      for(int i = 0; i < 5; i++) {
         int result = client.power2(i);

         System.out.println(i + "^2 == " + result);
         Thread.sleep(1000);
      }
      
      Person p = new Person();
      p.name = "Anna";
      p.address = "Finland";
      
      mgr.registerSerializer(Person.class, new PersonSerializer());
      PersonService client2 = mgr.createClient(PersonService.class);
      client2.checkPerson(p);
   }
}
