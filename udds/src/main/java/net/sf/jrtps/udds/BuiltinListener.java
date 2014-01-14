package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.builtin.PublicationData;

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
	
	protected void fireWriterMatched(DataReader<?> dr, PublicationData writerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.writerMatched(dr, writerData);
		}
	}

	protected void fireInconsistentQoS(DataReader<?> dr, PublicationData writerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.inconsistentQoS(dr, writerData);
		}
	}

	protected void fireReaderDetected(SubscriptionData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.readerDetected(readerData);
		}
	}
	
	protected void fireReaderMatched(DataWriter<?> dw, SubscriptionData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.readerMatched(dw, readerData);
		}
	}

	protected void fireInconsistentQoS(DataWriter<?> dw, SubscriptionData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.inconsistentQoS(dw, readerData);
		}
	}
}
