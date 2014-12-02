package net.sf.jrtps.udds;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.message.parameter.PermissionsToken;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.udds.security.KeyStoreAuthenticationService;

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
    private final KeyStoreAuthenticationService authPlugin;
	private boolean securityEnabled;

    BuiltinParticipantDataListener(Participant p, Map<GuidPrefix, ParticipantData> discoveredParticipants) {
        super(p);
        this.securityEnabled = p.getConfiguration().isSecurityEnabled();
        this.discoveredParticipants = discoveredParticipants;
        authPlugin = p.getAuthenticationService();
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
                        
                        if (authPlugin != null) {
                        	authenticate(pd);
                        }
                        
                        discoveredParticipants.put(pd.getGuidPrefix(), pd);

                        fireParticipantDetected(pd);

                        // First, add matched writers for builtin readers
                        handleBuiltinReaders(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
                        
                        // Then, make sure remote participant knows about us.
                        DataWriter<?> pw = participant.getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
                        SubscriptionData rd = new SubscriptionData(ParticipantData.BUILTIN_TOPIC_NAME,
                                ParticipantData.class.getName(), new Guid(pd.getGuidPrefix(),
                                        EntityId.SPDP_BUILTIN_PARTICIPANT_READER), pd.getQualityOfService());
                        if (pw == null) {
                            log.error("No SPDP writer in {}", participant.getWriters());
                        }
                        
                        pw.addMatchedReader(rd);

                        participant.waitFor(participant.getConfiguration().getSEDPDelay());
                        
                        // Then, add matched readers for builtin writers, 
                        // and announce our builtin endpoints
                        handleBuiltinWriters(pd.getGuidPrefix(), pd.getBuiltinEndpoints());
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

    private void authenticate(ParticipantData pd) {
    	log.debug("authenticate({})", pd.getGuidPrefix());
    	IdentityToken iToken = (IdentityToken) pd.getParameter(ParameterId.PID_IDENTITY_TOKEN);
    	if (iToken != null) {
    		PermissionsToken pToken = (PermissionsToken) pd.getParameter(ParameterId.PID_PERMISSIONS_TOKEN);
    		try {
    			CountDownLatch latch = null;
    			while((latch = authPlugin.doHandshake(iToken, pd.getBuiltinTopicKey())) != null) {
//    				boolean await = latch.await(2000, TimeUnit.MILLISECONDS);
//    				if (!await) {
//    					authPlugin.cancelHandshake(iToken);    					
//    				}
    			}
    		}
    		catch(Exception e) {
    			log.debug("Failed to validate remote Participant", pd.getGuidPrefix());
        		pd.setAuthenticated(false);
    		}
    	}
    	else {
    		log.debug("Remote participant {} did not send IdentityToken, mark it as unauthenticated",
    				pd.getGuidPrefix());
    		pd.setAuthenticated(false);
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
        log.debug("handleBuiltinEndpointSet {}", eps);

        if (eps.hasPublicationDetector()) {
            log.trace("Notifying remote publications reader of our publications");
            DataWriter<?> pw = participant.getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
            SubscriptionData rd = new SubscriptionData(PublicationData.BUILTIN_TOPIC_NAME,
                    PublicationData.class.getName(), key, sedpQoS);
            pw.addMatchedReader(rd);
        }
        if (eps.hasSubscriptionDetector()) {
            log.trace("Notifying remote subscriptions reader of our subscriptions");
            DataWriter<?> sw = participant.getWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);

            Guid key = new Guid(prefix, EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
            SubscriptionData rd = new SubscriptionData(SubscriptionData.BUILTIN_TOPIC_NAME,
                    SubscriptionData.class.getName(), key, sedpQoS);

            sw.addMatchedReader(rd);
        }
        if (eps.hasParticipantMessageReader()) {
            log.trace("Notifying remote participant message reader");
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
        log.debug("handleBuiltinReaders {}", eps);

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
}
