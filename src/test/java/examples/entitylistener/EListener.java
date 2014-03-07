package examples.entitylistener;

import java.io.IOException;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
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
    public void livelinessLost(PublicationData remoteWriter) {
        System.out.println("Liveliness lost: " + remoteWriter);
    }

    @Override
    public void livelinessLost(SubscriptionData remoteReader) {
        System.out.println("Liveliness lost: " + remoteReader);
    }

    @Override
    public void readerDetected(SubscriptionData rd) {
        System.out.println("Reader detected: " + rd);
    }

    @Override
    public void readerMatched(DataWriter<?> writer, SubscriptionData rd) {
        System.out.println("Reader matched: " + writer + ", " + rd);
    }

    @Override
    public void writerDetected(PublicationData wd) {
        System.out.println("Writer detected: " + wd);
    }

    @Override
    public void writerMatched(DataReader<?> reader, PublicationData wd) {
        System.out.println("Writer matched: " + reader + ", " + wd);
    }

    @Override
    public void inconsistentQoS(DataWriter<?> writer, SubscriptionData rd) {
        System.out.println("Inconsistent QoS: " + writer + ", " + rd);
    }

    @Override
    public void inconsistentQoS(DataReader<?> reader, PublicationData wd) {
        System.out.println("Inconsistent QoS: " + reader + ", " + wd);
    }
}
