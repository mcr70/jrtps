package net.sf.jrtps.aperf1;

import java.io.IOException;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class PSubscriber {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Configuration cfg = new Configuration("/jrtps-aperf1.properties");
		Participant p = new Participant(0, -1, null, cfg); // domain 0

        p.setMarshaller(Hello.class, new HelloMarshaller());

        DataReader<Hello> dr = p.createDataReader(Hello.class, new PQoS());
        HelloListener hl = new HelloListener();
        dr.addSampleListener(hl);

        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }
}
