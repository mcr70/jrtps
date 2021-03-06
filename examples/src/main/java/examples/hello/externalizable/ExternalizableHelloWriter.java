package examples.hello.externalizable;

import java.net.SocketException;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class ExternalizableHelloWriter {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0, 2); // domain 0, participant 2

        DataWriter<ExternalizableHelloMessage> dw = p.createDataWriter(ExternalizableHelloMessage.class);

        for (int i = 0; i < 5; i++) {
            ExternalizableHelloMessage m = new ExternalizableHelloMessage(i, "Hello");
            dw.write(m);
            Thread.sleep(1000);
        }

        p.close();
    }
}
