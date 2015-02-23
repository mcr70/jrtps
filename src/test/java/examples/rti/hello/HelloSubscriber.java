package examples.rti.hello;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class HelloSubscriber {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 1); // domain 0, participant 1

        p.setMarshaller(Hello.class, new HelloMarshaller());

        DataReader<Hello> dr = p.createDataReader(Hello.class);
        HelloListener hl = new HelloListener();
        dr.addSampleListener(hl);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }

}
