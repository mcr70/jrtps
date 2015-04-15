package examples.hello.serializable;

import java.io.IOException;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosPartition;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class HelloReader {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, -1, null, new Configuration("/jrtps1.properties")); 

        QualityOfService qos = new QualityOfService();
        qos.setPolicy(new QosPartition(new String[]{"partition-1"}));
        
        DataReader<HelloMessage> dr = p.createDataReader(HelloMessage.class, qos);
        HelloListener hl = new HelloListener(); // implements SampleListener
        dr.addSampleListener(hl);
        
        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();
        
        System.out.println("Received " + dr.getInstances().size() + " instances from writers.");
        System.out.println("total of " + dr.getSamples().size() + " samples available");
        
        p.close();
    }
}
