package net.sf.jrtps.udds;

import java.util.List;

import net.sf.jrtps.builtin.ParticipantData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParticipantLeaseManager tracks leaseTime of remote participants. If leaseTime is expired,
 * Local endpoints will be notified of this fact by removing matched readers/writers created
 * by that participant.  
 * 
 * @author mcr70
 */
class ParticipantLeaseManager implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ParticipantLeaseManager.class);
	private final List<ParticipantData> discoveredParticipants;
	private Participant participant;

	ParticipantLeaseManager(Participant participant, List<ParticipantData> discoveredParticipants) {
		this.participant = participant;
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			for (ParticipantData pd : discoveredParticipants) {
				if (pd.isLeaseExpired()) {
					// TODO: implement participants lease expiration
					log.warn("Lease expiration has not been implemented");
				}
			}
			
			long sleepTime = getNextSleepTime();
			running = participant.waitFor(sleepTime);
		}
		
		log.debug("ParticipantLeaseManager is exiting");
	}

	/**
	 * Gets the next LeaseTime. If there is no Participants detected, next sleepTime is 1 second.
	 * Otherwise, next sleepTime is smallest sleepTime of detected participants.
	 * 
	 * @return next sleepTime
	 */
	private long getNextSleepTime() {
		long smallest_expireTime = Long.MAX_VALUE;
		for (ParticipantData pd : discoveredParticipants) {
			if (pd.getLeaseExpirationTime() < smallest_expireTime) {
				smallest_expireTime = pd.getLeaseExpirationTime();
			}
		}

		if (smallest_expireTime == Long.MAX_VALUE) {
			smallest_expireTime = 1000;
		}

		return smallest_expireTime;
	}
}
