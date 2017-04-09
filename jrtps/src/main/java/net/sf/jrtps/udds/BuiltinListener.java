package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;

class BuiltinListener {
    protected final Participant participant;

    BuiltinListener(Participant p) {
        this.participant = p;

    }

    protected void fireParticipantDetected(ParticipantData pd) {
        for (EntityListener el : participant.getEntityListeners()) {
            el.participantDetected(pd);
        }
    }

    protected void fireParticipantLost(ParticipantData pd) {
        for (EntityListener el : participant.getEntityListeners()) {
            el.participantLost(pd);
        }
    }

    protected void fireWriterDetected(PublicationData writerData) {
        for (EntityListener el : participant.getEntityListeners()) {
            el.writerDetected(writerData);
        }
    }

    protected void fireReaderDetected(SubscriptionData readerData) {
        for (EntityListener el : participant.getEntityListeners()) {
            el.readerDetected(readerData);
        }
    }
}
