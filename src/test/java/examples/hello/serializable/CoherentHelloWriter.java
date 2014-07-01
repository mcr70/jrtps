package examples.hello.serializable;


import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class CoherentHelloWriter {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0); // domain 0, participant 2

        DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);
        
        List<HelloMessage> changes = new LinkedList<>();
        
        for (int i = 0; i < 10; i++) {
            changes.add(new HelloMessage(i , "Hello"));
        }
        
        Thread.sleep(2000); // Sleep. matchmaking needs to happen before write
        
        dw.write(changes);
        
        Thread.sleep(2000); // Sleep. Let readers fetch our data before closing

        System.out.println("Created " + dw.getInstances().size() + " instances. Closing Participant.");
        
        p.close();
    }
}
