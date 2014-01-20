package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParticipantLeaseManager tracks leaseTime of remote participants. If leaseTime is expired,
 * Local endpoints will be notified of this.
 * 
 * @author mcr70
 */
class ParticipantLeaseManager implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ParticipantLeaseManager.class);
	private final Participant participant;
	private final Map<GuidPrefix, ParticipantData> discoveredParticipants;


	ParticipantLeaseManager(Participant participant, Map<GuidPrefix, ParticipantData> discoveredParticipants) {
		this.participant = participant;
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			if (discoveredParticipants.size() > 0) {
				log.debug("Checking lease of {} remote participants", discoveredParticipants.size());
			}
			
			List<GuidPrefix> expiryList = new LinkedList<>();
			for (ParticipantData pd : discoveredParticipants.values()) {
				if (pd.isLeaseExpired()) {
					log.debug("Lease has expired for {}, currentTime is {}, expirationTime is {}", 
							pd.getGuidPrefix(), System.currentTimeMillis(), pd.getLeaseExpirationTime());
					expiryList.add(pd.getGuidPrefix());
				}
			}
			
			for (GuidPrefix prefix: expiryList) {
				participant.handleParticipantLeaseExpiration(prefix);
			}			
			
			long sleepTime = getNextSleepTime();
			//log.debug("Next sleep time is " + sleepTime);
			running = participant.waitFor(sleepTime);
		}
		
		log.debug("ParticipantLeaseManager is exiting, running={}", running);
	}

	/**
	 * Gets the next LeaseTime. If there is no Participants detected, next sleepTime is 1 second.
	 * Otherwise, next sleepTime is smallest sleepTime of detected participants.
	 * 
	 * @return next sleepTime
	 */
	private long getNextSleepTime() {
		if (discoveredParticipants.size() == 0) {
			return 1000; // TODO: configurable?
		}
		
		long smallest_expireTime = Long.MAX_VALUE;

		for (ParticipantData pd : discoveredParticipants.values()) {
			if (pd.getLeaseExpirationTime() < smallest_expireTime) {
				smallest_expireTime = pd.getLeaseExpirationTime();
			}
		}

		long nextSleeptime = smallest_expireTime - System.currentTimeMillis();

		if (nextSleeptime <= 0) {
			nextSleeptime = 1000; // TODO: configurable?
		}
		
		return nextSleeptime;
	}
}
