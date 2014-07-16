package examples.ospl.hello;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class Subscriber {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 1); // domain 0, participant 1

        p.setMarshaller(Msg.class, new MsgMarshaller());

        DataReader<Msg> dr = p.createDataReader(Msg.class, new MsgQoS());
        MsgListener hl = new MsgListener();
        dr.addSampleListener(hl);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }
}
