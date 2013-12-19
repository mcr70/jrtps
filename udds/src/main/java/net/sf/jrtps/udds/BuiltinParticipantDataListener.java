package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.RTPSWriter;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

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
	private Participant participant;

	private final HashMap<GuidPrefix, ParticipantData> discoveredParticipants;


	BuiltinParticipantDataListener(Participant p, HashMap<GuidPrefix, ParticipantData> discoveredParticipants) {
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
				if (pd.getGuidPrefix().equals(participant.getRTPSParticipant().getGuid().prefix)) {
					log.trace("Ignoring self");
				}
				else {
					log.debug("A new Participant detected: {}", pd);
					discoveredParticipants.put(pd.getGuidPrefix(), pd);

					// First, make sure remote participant knows about us.
					DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
					pw.getRTPSWriter().sendData(pd.getGuidPrefix(), EntityId.SPDP_BUILTIN_PARTICIPANT_READER, 0L);

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
	private void handleBuiltinEnpointSet(GuidPrefix prefix, int builtinEndpoints) {
		log.debug("handleBuiltinEndpointSet {}", builtinEndpoints);
		QualityOfService sedpQoS = new QualityOfService();
		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		} catch (InconsistentPolicy e) {
			log.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		
		if (eps.hasPublicationDetector()) {
			log.debug("Notifying remote publications reader of our publications");
			DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			
			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
			ReaderData rd = new ReaderData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			
			pw.getRTPSWriter().addMatchedReader(rd);
			//pw.getRTPSWriter().notifyReader(key);
			pw.getRTPSWriter().sendData(key.prefix, key.entityId, 0L);
		}
		if (eps.hasPublicationAnnouncer()) {
			DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);

			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			WriterData wd = new WriterData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			pr.getRTPSReader().addMatchedWriter(wd);
		}
		if (eps.hasSubscriptionDetector()) {
			log.debug("Notifying remote subscriptions reader of our subscriptions");
			DataWriter<?> sw = participant.getWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			
			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
			ReaderData rd = new ReaderData(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), key, sedpQoS);
			
			sw.getRTPSWriter().addMatchedReader(rd);
			sw.getRTPSWriter().notifyReader(key);
			//sw.sendData(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER, 0L);
		}
		if (eps.hasSubscriptionAnnouncer()) {
			DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);

			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			WriterData wd = new WriterData(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), key, sedpQoS);
			pr.getRTPSReader().addMatchedWriter(wd);
		}
	}
}
