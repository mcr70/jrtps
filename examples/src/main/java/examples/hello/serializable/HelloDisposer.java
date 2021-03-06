package examples.hello.serializable;

import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class HelloDisposer {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0, 3); // domain 0, participant 3

        DataWriter<HelloMessage> dw = p.createDataWriter(HelloMessage.class);

        List<HelloMessage> msgs = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            msgs.add(new HelloMessage(i, "hello_" + i));
        }

        dw.dispose(msgs); // Dispose all the HelloMessages on one call

        System.out.println("\n*** Waiting 2 seconds before closing ***\n");
        Thread.sleep(2000); // Give some time for the recipients to receive before closing

        p.close();
    }
}
