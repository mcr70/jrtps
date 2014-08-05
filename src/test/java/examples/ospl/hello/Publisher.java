package examples.ospl.hello;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class Publisher {
    public static void main(String[] args) throws Exception {
        Participant p = new Participant(0, 2); // domain 0, participant 2

        p.setMarshaller(Msg.class, new MsgMarshaller());

        DataWriter<Msg> dw = p.createDataWriter(Msg.class, new MsgQoS());

        System.out.println("Press Enter to write samples");
        System.in.read();
        
        for (int i = 0; i < 10; i++) {
            Msg m = new Msg(i, "message" + i);
            dw.write(m);
            Thread.sleep(1000);
        }

        p.close();
    }
}
