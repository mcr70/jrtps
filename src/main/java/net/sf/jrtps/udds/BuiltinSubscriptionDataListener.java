package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.SampleListener;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinSubscriptionDataListener extends BuiltinListener implements SampleListener<SubscriptionData> {
    private static final Logger log = LoggerFactory.getLogger(BuiltinSubscriptionDataListener.class);

    private Map<Guid, SubscriptionData> discoveredReaders;

    BuiltinSubscriptionDataListener(Participant p, Map<Guid, SubscriptionData> discoveredReaders) {
        super(p);
        this.discoveredReaders = discoveredReaders;
    }

    /**
     * Handle discovered SubscriptionData. If SubscriptionData represents an
     * user defined Reader, and this participant has a Writer for same topic,
     * send writers history cache to reader.
     * 
     * @param samples
     */
    @Override
    public void onSamples(List<Sample<SubscriptionData>> samples) {
        for (Sample<SubscriptionData> sdSample : samples) {
            SubscriptionData sd = sdSample.getData();

            Guid key = sd.getKey();
            if (discoveredReaders.put(key, sd) == null) {
                log.debug("Discovered a new subscription {} for topic {}, type {}", key, sd.getTopicName(),
                        sd.getTypeName());
                fireReaderDetected(sd);
            }

            List<DataWriter<?>> writers = participant.getWritersForTopic(sd.getTopicName());
            for (DataWriter<?> w : writers) {
                if (!w.getRTPSWriter().isMatchedWith(sd) && !sdSample.isDisposed()) {
                    // Not associated and sample is not a dispose -> do associate
                    QualityOfService requested = sd.getQualityOfService();
                    QualityOfService offered = w.getRTPSWriter().getQualityOfService();
                    log.trace("Check for compatible QoS for {} and {}", w.getRTPSWriter().getGuid().getEntityId(), sd
                            .getKey().getEntityId());

                    if (offered.isCompatibleWith(requested)) {
                        w.getRTPSWriter().addMatchedReader(sd);
                        fireReaderMatched(w, sd);
                    } else {
                        log.warn("Discovered reader had incompatible QoS with writer: {}, local writers QoS: {}", sd, w
                                .getRTPSWriter().getQualityOfService());
                        fireInconsistentQoS(w, sd);
                    }
                } else if (w.getRTPSWriter().isMatchedWith(sd) && sdSample.isDisposed()) {
                    // Associated and sample is dispose -> remove association
                    w.getRTPSWriter().removeMatchedReader(sd);
                }
            }
        }
    }
}
