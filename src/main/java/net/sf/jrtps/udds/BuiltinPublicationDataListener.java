package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinPublicationDataListener extends BuiltinListener implements SampleListener<PublicationData> {
    private static final Logger log = LoggerFactory.getLogger(BuiltinPublicationDataListener.class);

    private Map<Guid, PublicationData> discoveredWriters;

    BuiltinPublicationDataListener(Participant p, Map<Guid, PublicationData> discoveredWriters) {
        super(p);
        this.discoveredWriters = discoveredWriters;
    }

    @Override
    public void onSamples(List<Sample<PublicationData>> samples) {
        for (Sample<PublicationData> pdSample : samples) {
            PublicationData pd = pdSample.getData();

            Guid key = pd.getBuiltinTopicKey();
            if (discoveredWriters.put(key, pd) == null) {                
            	log.debug("Discovered a new publication {} for topic {}, type {}, QoS: {}", key, pd.getTopicName(),
                        pd.getTypeName(), pd.getQualityOfService());
                fireWriterDetected(pd);
            }

            List<DataReader<?>> readers = participant.getReadersForTopic(pd.getTopicName());
            log.debug("considering {} readers for topic {}, is disposed: {}", readers.size(), 
            		pd.getTopicName(), pdSample.isDisposed());
            
            for (DataReader<?> r : readers) {
                if (!pdSample.isDisposed()) {
                	// Try to associate 
                	QualityOfService offered = pd.getQualityOfService();
                    QualityOfService requested = r.getRTPSReader().getQualityOfService();
                    log.trace("Check for compatible QoS for {} and {}", pd.getBuiltinTopicKey().getEntityId(), 
                            r.getRTPSReader().getGuid().getEntityId());
                    
                    if (offered.isCompatibleWith(requested)) {
                        r.addMatchedWriter(pd);
                    } 
                    else {
                    	// Reader might have been previously associated. Remove association.
                    	r.removeMatchedWriter(pd);
                        log.warn("Discovered writer had incompatible QoS with reader. {}, {}", pd, 
                                r.getRTPSReader().getQualityOfService());
                        r.inconsistentQoS(pd);
                    }
                } 
                else {
                    log.debug("PublicationData was disposed, removing matched writer");
                    // Associated and sample is dispose -> remove association
                    r.removeMatchedWriter(pd);
                }
            }
            
            if (pdSample.isDisposed()) {
            	discoveredWriters.remove(key);
            }
        }
    }
}
