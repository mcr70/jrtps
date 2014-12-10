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
import net.sf.jrtps.udds.security.AuthenticationListener;
import net.sf.jrtps.udds.security.KeystoreAuthenticationPlugin;
import net.sf.jrtps.udds.security.ParticipantStatelessMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BuiltinListener keeps track of remote entities.
 * 
 * @author mcr70
 * 
 */
class BuiltinParticipantDataListener extends BuiltinListener 
implements SampleListener<ParticipantData>, AuthenticationListener {
	
    private static final Logger logger = LoggerFactory.getLogger(BuiltinParticipantDataListener.class);
    
    private final Map<GuidPrefix, ParticipantData> discoveredParticipants;
    private final KeystoreAuthenticationPlugin authPlugin;

    BuiltinParticipantDataListener(Participant p, Map<GuidPrefix, ParticipantData> discoveredParticipants) {
        super(p);
        this.discoveredParticipants = discoveredParticipants;
        authPlugin = p.getAuthenticationService();
        authPlugin.addAuthenticationListener(this);
    }

    @Override
    public void onSamples(List<Sample<ParticipantData>> samples) {
        for (Sample<ParticipantData> pdSample : samples) {
            ParticipantData pd = pdSample.getData();
            logger.debug("Considering Participant {}", pd.getGuidPrefix());

            ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
            if (d == null) {
                if (pd.getGuidPrefix() != null) {
                    if (pd.getGuidPrefix().equals(participant.getRTPSParticipant().getGuid().getPrefix())) {
                        logger.trace("Ignoring self");
                    } else {
                        logger.debug("A new Participant detected: {}, parameters received: {}", pd.getGuidPrefix(), pd.getParameters());
                        
                        discoveredParticipants.put(pd.getGuidPrefix(), pd);

                        fireParticipantDetected(pd);

                        // TODO: Having matching here allows remote writers to match
                        //       without authentication. Consider moving following line
                        //       to authenticationListener, if security is enabled.

                        // First, add matched writers for builtin readers
                        handleBuiltinReaders(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
                        
                        // Then, make sure remote participant knows about us.
                        DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
                        SubscriptionData rd = new SubscriptionData(ParticipantData.BUILTIN_TOPIC_NAME,
                                ParticipantData.class.getName(), new Guid(pd.getGuidPrefix(),
                                        EntityId.SPDP_BUILTIN_PARTICIPANT_READER), pd.getQualityOfService());                        
                        pw.addMatchedReader(rd);

                        if (authPlugin != null) {
                        	addMatchedEntitiesForAuth(pd);                        	
                        	authPlugin.beginHandshake(pd);
                        }
                        else {    // No authentication            
                        	participant.waitFor(participant.getConfiguration().getSEDPDelay());
                        
                        	// Then, add matched readers for builtin writers, 
                        	// and announce our builtin endpoints
                        	handleBuiltinWriters(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
                        }
                    }
                }
                else {
                    logger.warn("Discovered ParticipantData did not have a guid prefix");
                }
            } 
            else {
                logger.debug("Renewed lease for {}, new expiration time is {}", pd.getGuidPrefix(),
                        new Date(pd.getLeaseExpirationTime()));
                d.renewLease(); // TODO: Should we always store the new
                // ParticipantData to discoveredParticipants.
            }
        }
    }



	private void addMatchedEntitiesForAuth(ParticipantData pd) {
    	if (pd.getIdentityToken() != null) {
            SubscriptionData rd = new SubscriptionData(ParticipantStatelessMessage.BUILTIN_TOPIC_NAME,
                    ParticipantStatelessMessage.class.getName(), 
                    new Guid(pd.getGuidPrefix(), EntityId.BUILTIN_PARTICIPANT_STATELESS_READER), 
                    pd.getQualityOfService());                        
            participant.getWriter(EntityId.BUILTIN_PARTICIPANT_STATELESS_WRITER).addMatchedReader(rd);

            PublicationData wd = new PublicationData(ParticipantStatelessMessage.BUILTIN_TOPIC_NAME,
                    ParticipantStatelessMessage.class.getName(), 
                    new Guid(pd.getGuidPrefix(), EntityId.BUILTIN_PARTICIPANT_STATELESS_WRITER), 
                    pd.getQualityOfService());
            participant.getReader(EntityId.BUILTIN_PARTICIPANT_STATELESS_READER).addMatchedWriter(wd);
    	}
	}

	/**
     * Handle builtin endpoints for discovered participant. If participant has a
     * builtin reader for publications or subscriptions, send history cache to
     * them.
     * 
     * @param builtinEndpoints
     */
    private void handleBuiltinWriters(GuidPrefix prefix, int builtinEndpoints) {
        QualityOfService sedpQoS = QualityOfService.getSEDPQualityOfService();

        BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
        logger.debug("handleBuiltinEndpointSet {}", eps);

        if (eps.hasPublicationDetector()) {
            logger.trace("Notifying remote publications reader of our publications");
            DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
            SubscriptionData rd = new SubscriptionData(PublicationData.BUILTIN_TOPIC_NAME,
                    PublicationData.class.getName(), key, sedpQoS);
            pw.addMatchedReader(rd);
        }
        if (eps.hasSubscriptionDetector()) {
            logger.trace("Notifying remote subscriptions reader of our subscriptions");
            DataWriter<?> sw = participant.getWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
            SubscriptionData rd = new SubscriptionData(SubscriptionData.BUILTIN_TOPIC_NAME,
                    SubscriptionData.class.getName(), key, sedpQoS);

            sw.addMatchedReader(rd);
        }
        if (eps.hasParticipantMessageReader()) {
            logger.trace("Notifying remote participant message reader");
            DataWriter<?> sw = participant.getWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);

            Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);
            SubscriptionData rd = new SubscriptionData(ParticipantMessage.BUILTIN_TOPIC_NAME,
                    ParticipantMessage.class.getName(), key, sedpQoS);

            sw.addMatchedReader(rd);
        }
    }
    
    
    /**
     * Handle builtin readers for discovered participant. 
     * 
     * @param builtinEndpoints
     */
    private void handleBuiltinReaders(GuidPrefix prefix, int builtinEndpoints) {
        QualityOfService sedpQoS = QualityOfService.getSEDPQualityOfService();

        BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
        logger.debug("handleBuiltinReaders {}", eps);

        if (eps.hasPublicationAnnouncer()) {
            DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
            PublicationData wd = new PublicationData(PublicationData.BUILTIN_TOPIC_NAME,
                    PublicationData.class.getName(), key, sedpQoS);
            pr.addMatchedWriter(wd);
        }
        if (eps.hasSubscriptionAnnouncer()) {
            DataReader<?> pr = participant.getReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
            PublicationData wd = new PublicationData(SubscriptionData.BUILTIN_TOPIC_NAME,
                    SubscriptionData.class.getName(), key, sedpQoS);
            pr.addMatchedWriter(wd);
        }
        if (eps.hasParticipantMessageWriter()) {
            DataReader<?> pr = participant.getReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);

            Guid key = new Guid(prefix, EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
            PublicationData wd = new PublicationData(ParticipantMessage.BUILTIN_TOPIC_NAME,
                    ParticipantMessage.class.getName(), key, sedpQoS);
            pr.addMatchedWriter(wd);
        }
    }

	@Override
	public void authenticationSucceded(ParticipantData pd) {
		logger.debug("Authentication of {} succeeded", pd);
		participant.waitFor(participant.getConfiguration().getSEDPDelay());
    	
		// Once we are authenticated, match our builtin writers with remote
		// counterparts. This also announces our custom entities to remote
		// participant.
		handleBuiltinWriters(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
	}

	@Override
	public void authenticationFailed(ParticipantData pd) {
		logger.debug("Authentication of {} failed", pd.getGuidPrefix());
	}
}
