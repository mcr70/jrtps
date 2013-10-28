package net.sf.jrtps;

import java.util.HashMap;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Locator_t;
import net.sf.jrtps.types.Time_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BuiltinListener keeps track of remote entities.
 * 
 * @author mcr70
 *
 */
class BuiltinParticipantDataListener implements DataListener<ParticipantData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinParticipantDataListener.class);
	private RTPSParticipant participant;

	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;


	BuiltinParticipantDataListener(RTPSParticipant p, HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.participant = p;
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void onData(ParticipantData pd, Time_t timestamp, StatusInfo sInfo) {		
		log.trace("Considering Participant {}", pd.getGuid());
		
		ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
		if (d == null && pd.getGuidPrefix() != null) {
			if (pd.getGuidPrefix().equals(participant.guid.prefix)) {
				log.trace("Ignoring self");
			}
			else {
				log.debug("A new Participant detected: {}", pd); //.getGuidPrefix() + ", " + pd.getAllLocators());
				discoveredParticipants.put(pd.getGuidPrefix(), pd);
		
				// First, make sure remote participant knows about us.
				RTPSWriter pw = participant.getWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER);
				pw.sendHistoryCache(pd.getMetatrafficUnicastLocator(), EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER);
		
				// Then, announce our builtin endpoints
				handleBuiltinEnpointSet(pd.getBuiltinEndpoints(), pd.getMetatrafficUnicastLocator());
			}
		}
	}

	
	/**
	 * Handle builtin endpoints for discovered participant.
	 * If participant has a builtin reader for publications or subscriptions,
	 * send history cache to them.
	 * 
	 * @param builtinEndpoints
	 */
	private void handleBuiltinEnpointSet(int builtinEndpoints, Locator_t locator) {
		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		if (eps.hasPublicationDetector()) {
			//RTPSWriter pw = participant.getWriterForTopic("DCPSPublication");
			RTPSWriter pw = participant.getWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			pw.sendHistoryCache(locator, EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);
		}
		if (eps.hasSubscriptionDetector()) {
			//RTPSWriter pw = participant.getWriterForTopic("DCPSSubscription");
			RTPSWriter pw = participant.getWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			pw.sendHistoryCache(locator, EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
		}
	}
}
