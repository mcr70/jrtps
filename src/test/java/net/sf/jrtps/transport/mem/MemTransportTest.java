package net.sf.jrtps.transport.mem;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.transport.MemProvider;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class MemTransportTest {
    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration("/mem-test-1.properties");
        MemProvider mp = new MemProvider(cfg);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);
        
        // Create a participant for reader
        Participant p1 = new Participant(0, 0, null, cfg);

        DataReader<HelloMessage> dr = p1.createDataReader(HelloMessage.class);
        HelloListener hl = new HelloListener(); // implements DataListener
        dr.addSampleListener(hl);
        
        // Create a participant for writer
        Participant p2 = new Participant(0, 0, null, new Configuration("/mem-test-2.properties"));
        DataWriter<HelloMessage> dw = p2.createDataWriter(HelloMessage.class);
        
        int i = 0;
        while(i < 100) {
            HelloMessage m = new HelloMessage(i++ , "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }
                        
        System.out.println("Received " + dr.getInstances().size() + " instances from writers.");
        
        p1.close();
        p2.close();
    }
}
