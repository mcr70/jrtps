package net.sf.jrtps.transport.mem;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;
import examples.hello.serializable.HelloListener;
import examples.hello.serializable.HelloMessage;

public class MemTransportTest {
    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration("/jrtps-mem-test.properties");
        MemProvider mp = new MemProvider(cfg);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);
        
        // Create a participant for reader
        Participant p1 = new Participant(0, 0, null, cfg);

        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class);
        HelloListener hl = new HelloListener(); // implements DataListener
        dr.addSampleListener(hl);
        
        // Create a participant for writer
        Participant p2 = new Participant(0, 0, null, cfg);
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class);
        
        for (int i = 0; i < 10; i++) {
            HelloMessage m = new HelloMessage(i , "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }
        
        
        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();
        
        System.out.println("Received " + dr.getInstances().size() + " instances from writers.");
        
        p1.close();
    }

}
