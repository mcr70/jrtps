package net.sf.jrtps;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration_t;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BuiltinListener keeps track of remote entities.
 * 
 * @author mcr70
 *
 */
class BuiltinParticipantDataListener implements SampleListener<ParticipantData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinParticipantDataListener.class);
	private RTPSParticipant participant;

	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;


	BuiltinParticipantDataListener(RTPSParticipant p, HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.participant = p;
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void onSamples(List<Sample<ParticipantData>> samples) {
		for (Sample<ParticipantData> pdSample : samples) {
			ParticipantData pd = pdSample.getData();
			log.trace("Considering Participant {}", pd.getGuid());

			ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
			if (d == null && pd.getGuidPrefix() != null) {
				if (pd.getGuidPrefix().equals(participant.getGuid().prefix)) {
					log.trace("Ignoring self");
				}
				else {
					log.debug("A new Participant detected: {}", pd); //.getGuidPrefix() + ", " + pd.getAllLocators());
					discoveredParticipants.put(pd.getGuidPrefix(), pd);

					// First, make sure remote participant knows about us.
					RTPSWriter<?> pw = participant.getWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER);
					pw.sendData(pd.getGuidPrefix(), EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER, 0L);

					// Then, announce our builtin endpoints
					handleBuiltinEnpointSet(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
				}
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
	private void handleBuiltinEnpointSet(GuidPrefix_t prefix, int builtinEndpoints) {
		QualityOfService sedpQoS = new QualityOfService();
		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration_t(0, 0)));
		} catch (InconsistentPolicy e) {
			log.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		
		if (eps.hasPublicationDetector()) {
			RTPSWriter<?> pw = participant.getWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			
			GUID_t key = new GUID_t(prefix, EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);
			ReaderData rd = new ReaderData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			pw.addMatchedReader(rd);
			
			pw.sendData(prefix, EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER, 0L);
		}
		if (eps.hasPublicationAnnouncer()) {
			RTPSReader<?> pr = participant.getReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);

			GUID_t key = new GUID_t(prefix, EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			WriterData wd = new WriterData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			pr.addMatchedWriter(wd);
		}
		if (eps.hasSubscriptionDetector()) {
			RTPSWriter<?> sw = participant.getWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			
			GUID_t key = new GUID_t(prefix, EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
			ReaderData rd = new ReaderData(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), key, sedpQoS);
			sw.addMatchedReader(rd);
			
			sw.sendData(prefix, EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER, 0L);
		}
		if (eps.hasSubscriptionAnnouncer()) {
			RTPSReader<?> pr = participant.getReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);

			GUID_t key = new GUID_t(prefix, EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			WriterData wd = new WriterData(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), key, sedpQoS);
			pr.addMatchedWriter(wd);
		}
	}
}
