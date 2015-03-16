package examples.opendds.messenger;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class Publisher {
    public static void main(String[] args) throws Exception {
        Participant p = new Participant(0, 2); // domain 0, participant 2

        p.setMarshaller(Message.class, new MessageMarshaller());

        DataWriter<Message> dw = p.createDataWriter(Message.class, new MessengerQoS());

        for (int i = 0; i < 10; i++) {
            Message m = new Message("from foo" + i, "subject bar" + i, i, "text baz" + i, i);
            dw.write(m);
            Thread.sleep(1000);
        }

        p.close();
    }
}
