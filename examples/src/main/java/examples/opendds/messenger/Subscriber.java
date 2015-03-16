package examples.opendds.messenger;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class Subscriber {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 1); // domain 0, participant 1

        p.setMarshaller(Message.class, new MessageMarshaller());

        DataReader<Message> dr = p.createDataReader(Message.class, new MessengerQoS());
        MessageListener ml = new MessageListener();
        dr.addSampleListener(ml);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }
}
