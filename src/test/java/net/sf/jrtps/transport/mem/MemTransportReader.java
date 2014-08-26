package net.sf.jrtps.transport.mem;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;
import examples.hello.serializable.HelloListener;
import examples.hello.serializable.HelloMessage;

public class MemTransportReader {

    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration("/jrtps-mem-test.properties");
        MemProvider mp = new MemProvider(cfg);
        TransportProvider.registerTransportProvider("mem", mp, MemProvider.LOCATOR_KIND_MEM);
        
        Participant p = new Participant(0, 0, null, cfg);

        DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class);
        HelloListener hl = new HelloListener(); // implements DataListener
        dr.addSampleListener(hl);
        
        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();
        
        System.out.println("Received " + dr.getInstances().size() + " instances from writers.");
        
        p.close();
    }

}
