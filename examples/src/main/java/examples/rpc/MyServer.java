package examples.rpc;

import java.io.IOException;

import net.sf.jrtps.rpc.ServiceManager;

public class MyServer implements SampleService, PersonService {
   public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
      ServiceManager mgr = new ServiceManager();
      mgr.registerSerializer(Person.class, new PersonSerializer());
      
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

   @Override
   public void checkPerson(Person p) {
      System.out.println("Checking person: " + p.name + ", " + p.address);
   }
}
