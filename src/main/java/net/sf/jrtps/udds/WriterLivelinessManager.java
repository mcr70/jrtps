package net.sf.jrtps.udds;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.types.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LivelinessManager handles liveliness of writers.
 * <p>
 * 
 * Local writers that have QosLiveliness kind set to AUTOMATIC, will have their
 * liveliness automatically asserted by this. class. Local writers with
 * QosLiveliness kind set to MANUAL_BY_PARTICIPANT, will have their liveliness
 * asserted when application calls assertLiveliness() method of RTPSParticipant.
 * <p>
 * 
 * @author mcr70
 * @see net.sf.jrtps.RTPSParticipant#assertLiveliness()
 */
class WriterLivelinessManager implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WriterLivelinessManager.class);

    private final List<Duration> alDurations = new LinkedList<>();

    private final Participant participant;
    private DataWriter<ParticipantMessage> writer;

    private final ParticipantMessage manualSample;
    private final ParticipantMessage automaticSample;

    WriterLivelinessManager(Participant participant) {
        this.participant = participant;

        // Create samples used with liveliness protocol
        manualSample = new ParticipantMessage(participant.getRTPSParticipant().getGuid().getPrefix(),
                ParticipantMessage.MANUAL_LIVELINESS_KIND, new byte[0]);
        automaticSample = new ParticipantMessage(participant.getRTPSParticipant().getGuid().getPrefix(),
                ParticipantMessage.AUTOMATIC_LIVELINESS_KIND, new byte[0]);
    }

    /**
     * Asserts liveliness. This implements QosLiveliness: MANUAL_BY_PARTICIPANT.
     * Liveliness is asserted by writing a sample to
     * RTPSWriter<ParticipantMessage>.
     */
    void assertLiveliness() {
        log.debug("Asserting liveliness of DataWriters with QosLiveliness kind MANUAL_BY_PARTICIPANT");
        writer.write(manualSample);
    }

    /**
     * Register a writer. Writer is check for its QosLiveliness kind and if it
     * is AUTOMATIC, its lease_period is stored for usage with livelinessThread.
     * 
     * @param aWriter
     */
    void registerWriter(DataWriter<?> aWriter) {
        QosLiveliness policy = (QosLiveliness) aWriter.getRTPSWriter().getQualityOfService()
                .getPolicy(QosLiveliness.class);
        if (policy.getKind() == QosLiveliness.Kind.AUTOMATIC) {
            synchronized (alDurations) {
                log.debug("Registering DataWriter for automatic liveliness with lease duration of {}",
                        policy.getLeaseDuration());
                alDurations.add(policy.getLeaseDuration());
                Collections.sort(alDurations);
            }
        }
    }

    /**
     * Unregister a writer. Writer is check for its QosLiveliness kind and if it
     * is AUTOMATIC, its lease_period is removed from bookkeeping.
     * 
     * @param aWriter
     */
    void unregisterWriter(RTPSWriter<?> aWriter) {
        QosLiveliness policy = (QosLiveliness) aWriter.getQualityOfService().getPolicy(QosLiveliness.class);
        if (policy.getKind() == QosLiveliness.Kind.AUTOMATIC) {
            synchronized (alDurations) {
                alDurations.remove(policy.getLeaseDuration());
            }
        }
    }

    /**
     * Starts livelinessThread.
     */
    void start() {
        writer = participant.getDataWriter(ParticipantMessage.class);

        log.debug("Starting liveliness thread");
        participant.addRunnable(this);
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            Duration nextLeaseWaitTime = null;
            synchronized (alDurations) {
                if (alDurations.size() > 0) {
                    nextLeaseWaitTime = alDurations.get(0);
                }
            }

            // log.debug("Next lease wait time is {}", nextLeaseWaitTime);

            long sleepTime = 0;
            if (nextLeaseWaitTime != null) { // We have at least one writer to
                                             // assert liveliness for
                log.trace("Asserting liveliness of RTPSWriters with QosLiveliness kind AUTOMATIC");
                writer.write(automaticSample);
                sleepTime = nextLeaseWaitTime.asMillis();
            }
            if (sleepTime <= 0) {
                sleepTime = 10000; // TODO: hardcoded.
            }

            running = participant.waitFor(sleepTime);
        }

        log.debug("WriterLivelinessManager is exiting");
    }
}
