package examples.rpc;

import net.sf.jrtps.rpc.Service;

public interface SampleService extends Service {
   void foo(int i);
}
