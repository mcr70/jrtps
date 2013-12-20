package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;

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
	
	protected void fireWriterDetected(WriterData writerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.writerDetected(writerData);
		}
	}
	
	protected void fireWriterMatched(DataReader<?> dr, WriterData writerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.writerMatched(dr, writerData);
		}
	}

	protected void fireInconsistentQoS(DataReader<?> dr, WriterData writerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.inconsistentQoS(dr, writerData);
		}
	}

	protected void fireReaderDetected(ReaderData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.readerDetected(readerData);
		}
	}
	
	protected void fireReaderMatched(DataWriter<?> dw, ReaderData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.readerMatched(dw, readerData);
		}
	}

	protected void fireInconsistentQoS(DataWriter<?> dw, ReaderData readerData) {
		for (EntityListener el : participant.getEntityListeners()) {
			el.inconsistentQoS(dw, readerData);
		}
	}
}
