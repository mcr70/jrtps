package net.sf.jrtps.udds;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.RTPSWriter;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.ParticipantMessageMarshaller;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LivelinessManager handles liveliness of writers.<p>
 * 
 * Local writers that have QosLiveliness kind set to AUTOMATIC, will have their liveliness
 * automatically asserted by this. class. Local writers with QosLiveliness kind set to
 * MANUAL_BY_PARTICIPANT, will have their liveliness asserted when application calls
 * assertLiveliness() method of RTPSParticipant.<p>
 * 
 * This class is also a SampleListener of ParticipantMessages. Remote writers liveliness is 
 * asserted when samples arrive. 
 * 
 * @author mcr70
 * @see net.sf.jrtps.RTPSParticipant#assertLiveliness()
 */
class LivelinessManager implements Runnable, SampleListener<ParticipantMessage> {
	private static final Logger log = LoggerFactory.getLogger(LivelinessManager.class);
	
	private final List<Duration> alDurations = new LinkedList<>();
	
	private final Participant participant;
	private DataWriter<ParticipantMessage> writer;
	private DataReader<ParticipantMessage> reader;

	private final ParticipantMessage manualSample;
	private final ParticipantMessage automaticSample;

	
	public LivelinessManager(Participant participant) {
		this.participant = participant;

		// Create samples used with liveliness protocol
		manualSample = new ParticipantMessage(participant.getRTPSParticipant().getGuid().prefix, 
				ParticipantMessage.MANUAL_LIVELINESS_KIND, new byte[0]);
		automaticSample = new ParticipantMessage(participant.getRTPSParticipant().getGuid().prefix,
				ParticipantMessage.AUTOMATIC_LIVELINESS_KIND, new byte[0]);
	}
	
	/**
	 * Asserts liveliness. This implements QosLiveliness: MANUAL_BY_PARTICIPANT.
	 * Liveliness is asserted by writing a sample to RTPSWriter<ParticipantMessage>.
	 */
	void assertLiveliness() {
		log.debug("Asserting liveliness of DataWriters with QosLiveliness kind MANUAL_BY_PARTICIPANT");
		writer.write(manualSample);
	}
	
	/**
	 * Register a writer. Writer is check for its QosLiveliness kind and if it is AUTOMATIC,
	 * its lease_period is stored for usage with livelinessThread.
	 * 
	 * @param aWriter
	 */
	void registerWriter(DataWriter<?> aWriter) {
		QosLiveliness policy = (QosLiveliness) aWriter.getRTPSWriter().getQualityOfService().getPolicy(QosLiveliness.class);
		if (policy.getKind() == QosLiveliness.Kind.AUTOMATIC) {
			synchronized (alDurations) {
				log.debug("Registering DataWriter for automatic liveliness with lease duration of ", 
						policy.getLeaseDuration());
				alDurations.add(policy.getLeaseDuration());
				Collections.sort(alDurations);
			}
		}
	}
	
	/**
	 * Unregister a writer. Writer is check for its QosLiveliness kind and if it is AUTOMATIC,
	 * its lease_period is removed from bookkeeping.
	 * 
	 * @param aWriter
	 */
	void unregisterWriter(RTPSWriter<?> aWriter) {
		QosLiveliness policy = (QosLiveliness) aWriter.getQualityOfService().getPolicy(QosLiveliness.class);
		if (policy.getKind() == QosLiveliness.Kind.AUTOMATIC) {
			synchronized (alDurations) {
				alDurations.remove(policy.getLeaseDuration());
			}
		}		
	}
	
	/**
	 * Starts livelinessThread.
	 */
	void start() {
		// ---  Create QualityOfService used with entities  ----- 
		QualityOfService qos = new QualityOfService();
		try {
			qos.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
			qos.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT_LOCAL));
			qos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));
		} catch (InconsistentPolicy e) {
			log.error("Got InconsistentPolicy. This is an internal error", e);
			throw new RuntimeException(e);
		}
		
		writer = participant.createDataWriter(ParticipantMessage.BUILTIN_TOPIC_NAME, ParticipantMessage.class, 
				ParticipantMessage.class.getName(), qos);

		reader = participant.createDataReader(ParticipantMessage.BUILTIN_TOPIC_NAME, ParticipantMessage.class, 
				ParticipantMessage.class.getName(), qos);
		reader.addListener(this);

		log.debug("Starting liveliness thread");
		participant.addRunnable(this);
	}

	@Override
	public void run() {
		try {
			while(true) {
				Duration nextLeaseWaitTime = null;
				synchronized (alDurations) {
					if (alDurations.size() > 0) {
						nextLeaseWaitTime = alDurations.get(0);
					}					
				}
			
				long sleepTime = 1000; // TODO: hardcoded. default sleep time if no writers present
				if (nextLeaseWaitTime != null) { // We have at least one writer to assert liveliness for
					log.debug("Asserting liveliness of RTPSWriters with QosLiveliness kind AUTOMATIC");
					writer.write(automaticSample);
					sleepTime = nextLeaseWaitTime.asMillis();
				}
				
				Thread.sleep(sleepTime);
			}
		}
		catch(InterruptedException ie) {
			log.debug("livelinessThread was interrupted");
		}
	}

	@Override
	public void onSamples(List<Sample<ParticipantMessage>> samples) {
		for (Sample<ParticipantMessage> sample : samples) {
			ParticipantMessage pm = sample.getData();
			// TODO: assert liveliness of remote writers
			log.warn("Liveliness message not handled");
		}
	}
}
