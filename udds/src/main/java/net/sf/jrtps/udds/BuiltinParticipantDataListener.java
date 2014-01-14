package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.SEDPQualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
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
class BuiltinParticipantDataListener extends BuiltinListener implements SampleListener<ParticipantData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinParticipantDataListener.class);

	private final Map<GuidPrefix, ParticipantData> discoveredParticipants;


	BuiltinParticipantDataListener(Participant p, Map<GuidPrefix, ParticipantData> discoveredParticipants) {
		super(p);
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void onSamples(List<Sample<ParticipantData>> samples) {
		log.debug("Got {} ParticipantData samples, known participants: {}", samples.size(), discoveredParticipants.keySet());
		for (Sample<ParticipantData> pdSample : samples) {
			ParticipantData pd = pdSample.getData();
			log.debug("Considering Participant {}", pd.getGuid());

			ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
			if (d == null && pd.getGuidPrefix() != null) {
				if (pd.getGuidPrefix().equals(participant.getRTPSParticipant().getGuid().getPrefix())) {
					log.trace("Ignoring self");
				}
				else {
					log.debug("A new Participant detected: {}, {}, current list of participants: {}", pd, pd.getQualityOfService(), discoveredParticipants);
					discoveredParticipants.put(pd.getGuidPrefix(), pd);
					
					fireParticipantDetected(pd);
					
					// First, make sure remote participant knows about us.
					DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
					ReaderData rd = 
							new ReaderData(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), 
									new Guid(pd.getGuidPrefix(), EntityId.SPDP_BUILTIN_PARTICIPANT_READER), 
									pd.getQualityOfService());
					pw.getRTPSWriter().addMatchedReader(rd);

					// Then, announce our builtin endpoints
					handleBuiltinEnpointSet(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
					
					log.debug("Discovered participants: {}, {}", hashCode(), discoveredParticipants.keySet());
				}
			}
			else {
				log.debug("Renewed lease for {}, new expiration time is {}", pd.getGuidPrefix(), pd.getLeaseExpirationTime());
				d.renewLease(); // TODO: Should we always store the new ParticipantData to discoveredParticipants.
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
		QualityOfService sedpQoS = new SEDPQualityOfService();

		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		log.debug("handleBuiltinEndpointSet {}", eps);
		
		if (eps.hasPublicationDetector()) {
			log.debug("Notifying remote publications reader of our publications");
			DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			
			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
			ReaderData rd = new ReaderData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			pw.getRTPSWriter().addMatchedReader(rd);
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
		}
		if (eps.hasSubscriptionAnnouncer()) {
			DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);

			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			WriterData wd = new WriterData(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), key, sedpQoS);
			pr.getRTPSReader().addMatchedWriter(wd);
		}
		if (eps.hasParticipantMessageReader()) {
			log.debug("Notifying remote participant data reader");
			DataWriter<?> sw = participant.getWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
			
			Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);
			ReaderData rd = new ReaderData(ParticipantMessage.BUILTIN_TOPIC_NAME, 
					ParticipantMessage.class.getName(), key, sedpQoS);
			
			sw.getRTPSWriter().addMatchedReader(rd);
		}
		if (eps.hasParticipantMessageWriter()) {
			DataReader<?> pr = participant.getReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);

			Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
			WriterData wd = new WriterData(ParticipantMessage.BUILTIN_TOPIC_NAME, 
					ParticipantMessage.class.getName(), key, sedpQoS);
			pr.getRTPSReader().addMatchedWriter(wd);
		}
	}
}
