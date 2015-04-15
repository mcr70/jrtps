package examples.entitylistener;

import java.io.IOException;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.udds.Participant;

public class EListener implements net.sf.jrtps.udds.EntityListener {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        Participant p = new Participant(0, 4); // domain 0, participant 4

        p.addEntityListener(new EListener());
        System.out.println("\n*** Press enter to close Participant ***\n");
        System.in.read();

        p.close();
    }

    @Override
    public void participantDetected(ParticipantData pd) {
        System.out.println("Participant detected: " + pd);
    }

    @Override
    public void participantLost(ParticipantData pd) {
        System.out.println("Participant lost: " + pd);
    }

    @Override
    public void readerDetected(SubscriptionData rd) {
        System.out.println("Reader detected: " + rd);
    }

    @Override
    public void writerDetected(PublicationData wd) {
        System.out.println("Writer detected: " + wd);
    }
}
