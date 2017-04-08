package examples.rpc;

import net.sf.jrtps.rpc.Service;

public interface ComplexService extends Service {
   void checkPerson(Person p);
}
