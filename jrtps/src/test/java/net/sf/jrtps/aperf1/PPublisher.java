package net.sf.jrtps.aperf1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class PPublisher {
    public static void main(String[] args) throws Exception {
        Configuration cfg = new Configuration("/jrtps-aperf1.properties");
		Participant p = new Participant(0, -1, null, cfg); // domain 0

        p.setMarshaller(Hello.class, new HelloMarshaller());

        CountDownLatch cdl = new CountDownLatch(1);
        DataWriter<Hello> dw = p.createDataWriter(Hello.class, new PQoS());
        dw.addCommunicationListener(new PCommunicationListener(cdl));

        boolean await = cdl.await(20000, TimeUnit.MILLISECONDS);
        if (!await) {
        	System.out.println("Failed to match with reader on time");
        	System.exit(1);
        }
        
        for (int i = 0; i < 100; i++) {
            Hello h = new Hello("hello " + i);
            dw.write(h);
            //Thread.sleep(1000);
        }

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();
        
        p.close();
    }
}
