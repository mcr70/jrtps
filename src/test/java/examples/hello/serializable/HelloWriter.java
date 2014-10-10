package examples.hello.serializable;

import java.net.SocketException;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosPartition;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class HelloWriter {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0); // domain 0, participant 2
        
        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosPartition(new String[]{"partition-1"}));

        // Remove from comments to see Lifespan policy to remove samples from reader 
        // qos.setPolicy(new QosLifespan(100)); 
        
        DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class, qos);
        
        for (int i = 0; i < 10; i++) {
            HelloMessage m = new HelloMessage(i , "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }

        System.out.println("Created " + dw.getInstances().size() + " instances. Closing Participant.");
        
        p.close();
    }
}
