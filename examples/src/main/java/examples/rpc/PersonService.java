package examples.rpc;

import net.sf.jrtps.rpc.Service;

public interface PersonService extends Service {
   void checkPerson(Person p);
}
