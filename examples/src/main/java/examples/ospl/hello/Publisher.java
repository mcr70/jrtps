package examples.ospl.hello;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class Publisher {
    public static void main(String[] args) throws Exception {
        Participant p = new Participant(0); // domain 0

        p.setMarshaller(Msg.class, new MsgMarshaller());

        DataWriter<Msg> dw = p.createDataWriter(Msg.class, new MsgQoS());
        
        for (int i = 0; i < 100; i++) {
            Msg m = new Msg(i, "message" + i);
            dw.write(m);
            Thread.sleep(1000);
        }

        p.close();
    }
}
