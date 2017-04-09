package examples.rpc;

import net.sf.jrtps.rpc.Service;

public interface SampleService extends Service {
   int power2(int i);
}
