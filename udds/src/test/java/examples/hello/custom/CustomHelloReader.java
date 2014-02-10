package examples.hello.custom;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class CustomHelloReader {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 1); // Create participant; domain 0,
                                               // participant 2
        p.setMarshaller(CustomHelloMessage.class, new CustomMarshaller());

        DataReader<CustomHelloMessage> dr = p.createDataReader(CustomHelloMessage.class);
        CustomHelloListener hl = new CustomHelloListener(); // implements
                                                            // DataListener
        dr.addListener(hl);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }
}
