package net.sf.jrtps.transport.mem;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;
import examples.hello.serializable.HelloMessage;

public class MemTransportWriter {
    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration("/jrtps-mem-test.properties");
        MemProvider mp = new MemProvider(cfg);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);
        
        Participant p = new Participant(0, 0, null, cfg);

        DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);
        
        for (int i = 0; i < 10; i++) {
            HelloMessage m = new HelloMessage(i , "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }

        System.out.println("Created " + dw.getInstances().size() + " instances. Closing Participant.");
        
        p.close();
    }
}
