package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
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
class BuiltinParticipantDataListener extends BuiltinListener implements SampleListener<ParticipantData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinParticipantDataListener.class);

	private final HashMap<GuidPrefix, ParticipantData> discoveredParticipants;


	BuiltinParticipantDataListener(Participant p, HashMap<GuidPrefix, ParticipantData> discoveredParticipants) {
		super(p);
		this.discoveredParticipants = discoveredParticipants;
	}

	@Override
	public void onSamples(List<Sample<ParticipantData>> samples) {
		for (Sample<ParticipantData> pdSample : samples) {
			ParticipantData pd = pdSample.getData();
			log.trace("Considering Participant {}", pd.getGuid());

			ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
			if (d == null && pd.getGuidPrefix() != null) {
				if (pd.getGuidPrefix().equals(participant.getRTPSParticipant().getGuid().getPrefix())) {
					log.trace("Ignoring self");
				}
				else {
					log.debug("A new Participant detected: {}", pd);
					discoveredParticipants.put(pd.getGuidPrefix(), pd);
					fireParticipantDetected(pd);
					
					// First, make sure remote participant knows about us.
					DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
					//ReaderData rd = 
					//		new ReaderData(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), 
					//				pd.getGuid(), pd.getQualityOfService());
					//pw.getRTPSWriter().addMatchedReader(rd);
					//pw.getRTPSWriter().notifyReader(pd.getGuid());
					pw.getRTPSWriter().sendData(pd.getGuidPrefix(), EntityId.SPDP_BUILTIN_PARTICIPANT_READER, 0L);

					// Then, announce our builtin endpoints
					handleBuiltinEnpointSet(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
				}
			}
			else {
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
		QualityOfService sedpQoS = new QualityOfService();
		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		} catch (InconsistentPolicy e) {
			log.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		log.debug("handleBuiltinEndpointSet {}", eps);
		
		if (eps.hasPublicationDetector()) {
			log.debug("Notifying remote publications reader of our publications");
			DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			
			Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
			//ReaderData rd = new ReaderData(WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), key, sedpQoS);
			//pw.getRTPSWriter().addMatchedReader(rd);
			//pw.getRTPSWriter().notifyReader(key);
			pw.getRTPSWriter().sendData(key.getPrefix(), key.getEntityId(), 0L);
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
		if (eps.hasParticipantMessageReader()) {
			log.debug("Notifying remote participant data reader");
			DataWriter<?> sw = participant.getWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
			
			Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);
			ReaderData rd = new ReaderData(ParticipantMessage.BUILTIN_TOPIC_NAME, 
					ParticipantMessage.class.getName(), key, sedpQoS);
			
			sw.getRTPSWriter().addMatchedReader(rd);
			sw.getRTPSWriter().notifyReader(key);
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
