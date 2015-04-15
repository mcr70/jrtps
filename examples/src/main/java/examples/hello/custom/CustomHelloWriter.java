package examples.hello.custom;

import java.net.SocketException;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class CustomHelloWriter {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0, 2); // domain 0, participant 2
        
        p.setMarshaller(CustomHelloMessage.class, new CustomMarshaller());

        DataWriter<CustomHelloMessage> dw = p.createDataWriter(CustomHelloMessage.class);

        for (int i = 0; i < 10; i++) {
            CustomHelloMessage m = new CustomHelloMessage(i, "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }

        p.close();
    }
}
