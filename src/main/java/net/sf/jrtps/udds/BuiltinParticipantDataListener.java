package net.sf.jrtps.udds;

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.rtps.Sample;
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
        for (Sample<ParticipantData> pdSample : samples) {
            ParticipantData pd = pdSample.getData();
            log.debug("Considering Participant {}", pd.getGuidPrefix());

            ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
            if (d == null) {
                if (pd.getGuidPrefix() != null) {
                    if (pd.getGuidPrefix().equals(participant.getRTPSParticipant().getGuid().getPrefix())) {
                        log.trace("Ignoring self");
                    } else {
                        log.debug("A new Participant detected: {}, parameters received: {}", pd.getGuidPrefix(), pd.getParameters());
                        discoveredParticipants.put(pd.getGuidPrefix(), pd);

                        fireParticipantDetected(pd);

                        // First, make sure remote participant knows about us.
                        DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
                        SubscriptionData rd = new SubscriptionData(ParticipantData.BUILTIN_TOPIC_NAME,
                                ParticipantData.class.getName(), new Guid(pd.getGuidPrefix(),
                                        EntityId.SPDP_BUILTIN_PARTICIPANT_READER), pd.getQualityOfService());
                        pw.getRTPSWriter().addMatchedReader(rd);

                        // Then, announce our builtin endpoints
                        handleBuiltinEnpointSet(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
                    }
                }
                else {
                    log.warn("Discovered ParticipantData did not have a guid prefix");
                }
            } 
            else {
                log.debug("Renewed lease for {}, new expiration time is {}", pd.getGuidPrefix(),
                        new Date(pd.getLeaseExpirationTime()));
                d.renewLease(); // TODO: Should we always store the new
                // ParticipantData to discoveredParticipants.
            }
        }
    }

    /**
     * Handle builtin endpoints for discovered participant. If participant has a
     * builtin reader for publications or subscriptions, send history cache to
     * them.
     * 
     * @param builtinEndpoints
     */
    private void handleBuiltinEnpointSet(GuidPrefix prefix, int builtinEndpoints) {
        QualityOfService sedpQoS = QualityOfService.getSEDPQualityOfService();

        BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
        log.debug("handleBuiltinEndpointSet {}", eps);

        if (eps.hasPublicationDetector()) {
            log.trace("Notifying remote publications reader of our publications");
            DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
            SubscriptionData rd = new SubscriptionData(PublicationData.BUILTIN_TOPIC_NAME,
                    PublicationData.class.getName(), key, sedpQoS);
            pw.getRTPSWriter().addMatchedReader(rd);
        }
        if (eps.hasPublicationAnnouncer()) {
            DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
            PublicationData wd = new PublicationData(PublicationData.BUILTIN_TOPIC_NAME,
                    PublicationData.class.getName(), key, sedpQoS);
            pr.getRTPSReader().addMatchedWriter(wd);
        }
        if (eps.hasSubscriptionDetector()) {
            log.trace("Notifying remote subscriptions reader of our subscriptions");
            DataWriter<?> sw = participant.getWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
            SubscriptionData rd = new SubscriptionData(SubscriptionData.BUILTIN_TOPIC_NAME,
                    SubscriptionData.class.getName(), key, sedpQoS);

            sw.getRTPSWriter().addMatchedReader(rd);
        }
        if (eps.hasSubscriptionAnnouncer()) {
            DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
            PublicationData wd = new PublicationData(SubscriptionData.BUILTIN_TOPIC_NAME,
                    SubscriptionData.class.getName(), key, sedpQoS);
            pr.getRTPSReader().addMatchedWriter(wd);
        }
        if (eps.hasParticipantMessageReader()) {
            log.trace("Notifying remote participant message reader");
            DataWriter<?> sw = participant.getWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);

            Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);
            SubscriptionData rd = new SubscriptionData(ParticipantMessage.BUILTIN_TOPIC_NAME,
                    ParticipantMessage.class.getName(), key, sedpQoS);

            sw.getRTPSWriter().addMatchedReader(rd);
        }
        if (eps.hasParticipantMessageWriter()) {
            DataReader<?> pr = participant.getReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);

            Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
            PublicationData wd = new PublicationData(ParticipantMessage.BUILTIN_TOPIC_NAME,
                    ParticipantMessage.class.getName(), key, sedpQoS);
            pr.getRTPSReader().addMatchedWriter(wd);
        }
    }
}
