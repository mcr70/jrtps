package examples.hello.externalizable;

import java.io.IOException;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class ExternalizableHelloReader {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 1); // domain 0, participant 1

        DataReader<ExternalizableHelloMessage> dr = p.createDataReader(ExternalizableHelloMessage.class);
        ExternalizableHelloListener hl = new ExternalizableHelloListener(); // implements SampleListener

        dr.addListener(hl);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }
}
