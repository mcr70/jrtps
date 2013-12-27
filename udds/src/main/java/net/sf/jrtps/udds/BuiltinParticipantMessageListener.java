package net.sf.jrtps.udds;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.WriterProxy;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosLiveliness.Kind;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BuiltinParticipantMessageListener handles incoming ParticipantMessages. 
 * ParticipantMessages are used to implement writer liveliness protocol.
 * 
 * @see ParticipantMessage
 * @author mcr70
 */
class BuiltinParticipantMessageListener extends BuiltinListener implements SampleListener<ParticipantMessage> {
	private static final Logger logger = LoggerFactory.getLogger(BuiltinParticipantMessageListener.class);
	private final List<DataReader<?>> localReaders;


	BuiltinParticipantMessageListener(Participant p, List<DataReader<?>> localReaders) {
		super(p);
		this.localReaders = localReaders;
	}

	@Override
	public void onSamples(List<Sample<ParticipantMessage>> samples) {
		for (Sample<ParticipantMessage> pmSample : samples) {
			ParticipantMessage pm = pmSample.getData();
			GuidPrefix guidPrefix = pm.getGuidPrefix();

			for (DataReader<?> dr : localReaders) {
				Collection<WriterProxy> matchedWriters = dr.getRTPSReader().getMatchedWriters(guidPrefix);
				for (WriterProxy wp : matchedWriters) {
					QualityOfService qos = wp.getWriterData().getQualityOfService();
					QosLiveliness qosLiveliness = (QosLiveliness) qos.getPolicy(QosLiveliness.class);

					if (qosLiveliness.getKind() == Kind.AUTOMATIC && pm.isAutomaticLivelinessKind()) {
						logger.trace("Asserting automatic liveliness of {}", wp.getGuid());
						wp.assertLiveliness();	
					}
					else if (qosLiveliness.getKind() == Kind.MANUAL_BY_PARTICIPANT && pm.isManualLivelinessKind()) {
						logger.trace("Asserting manual liveliness of {}", wp.getGuid());
						wp.assertLiveliness();	
					}
				}
			}
		}
	}
}
