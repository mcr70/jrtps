package examples.ospl.hello;

import java.io.IOException;
import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class Subscriber2 {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0); // domain 0

        p.setMarshaller(Msg.class, new MsgMarshaller());

        DataReader<Msg> dr = p.createDataReader(Msg.class, new MsgQoS());

        System.out.println("\n*** Press enter to read samples and close participant ***\n");
        System.in.read();

        List<Sample<Msg>> samples = dr.getSamples(); // Get all the samples we have received so far
        System.out.println("Read " + samples);

        p.close();
    }
}
