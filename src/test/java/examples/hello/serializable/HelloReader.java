package examples.hello.serializable;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class HelloReader {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0); // domain 0, participant 1

        DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class);
        HelloListener hl = new HelloListener(); // implements DataListener
        dr.addSampleListener(hl);
        
        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();
        
        System.out.println("Received " + dr.getInstances().size() + " instances from writers.");
        
        p.close();
    }
}
