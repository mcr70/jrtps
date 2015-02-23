package examples.rti.hello;

import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class HelloPublisher {
    public static void main(String[] args) throws Exception {
        Participant p = new Participant(0); // domain 0

        p.setMarshaller(Hello.class, new HelloMarshaller());

        DataWriter<Hello> dw = p.createDataWriter(Hello.class);
        
        for (int i = 0; i < 10; i++) {
            Hello h = new Hello("hello " + i);
            dw.write(h);
            Thread.sleep(1000);
        }

        p.close();
    }

}
