package examples.entityfactory;

import java.net.SocketException;

import net.sf.jrtps.udds.Participant;

public class HelloWriter {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketException,
            InterruptedException {
        Participant p = new Participant(0, 2, new CustomEntityFactory(), null);

        CustomDataWriter<HelloMessage> cdw = (CustomDataWriter<HelloMessage>) p.createDataWriter(HelloMessage.class);
        cdw.write(new HelloMessage(1, "hello"), System.currentTimeMillis());
        
        p.close();
    }
}
