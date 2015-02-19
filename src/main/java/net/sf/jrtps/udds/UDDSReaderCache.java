package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.ContentFilterInfo;
import net.sf.jrtps.message.parameter.QosDestinationOrder.Kind;
import net.sf.jrtps.message.parameter.QosLifespan;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.ReaderCache;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterProxy;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.types.Time;
import net.sf.jrtps.util.Watchdog;
import net.sf.jrtps.util.Watchdog.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UDDSReaderCache<T> extends UDDSHistoryCache<T, PublicationData> implements ReaderCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(UDDSReaderCache.class);

    private Map<Guid, List<Sample<T>>> coherentSets = new HashMap<>(); // Used by reader
    private final Kind destinationOrderKind;
    private final Map<Integer, List<Sample<T>>> incomingSamples = new HashMap<>();
    private final boolean exclusiveOwnership;

    private RTPSReader<T> rtps_reader;

	private ContentFilter<T> sampleFilter;

	private byte[] filterSignature;


    UDDSReaderCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog) {
        super(eId, marshaller, qos, watchdog, true);

        destinationOrderKind = qos.getDestinationOrder().getKind();
        exclusiveOwnership = qos.getOwnership().getKind() == QosOwnership.Kind.EXCLUSIVE;
    }

    /**
     * Sets RTPSReader associated with this cache. RTPSReader is used to
     *  
     * @param rtps_reader
     */
    void setRTPSReader(RTPSReader<T> rtps_reader) {
        this.rtps_reader = rtps_reader;
    }

    
    void setSampleFilter(ContentFilter<T> sf) {
    	this.sampleFilter = sf;
    }

    @Override
    public Sample<T> addSample(final Sample<T> aSample) {
        if (rtps_reader != null) { // TODO: can be null on junit tests 
            WriterProxy matchedWriter = rtps_reader.getMatchedWriter(aSample.getWriterGuid());
            if (matchedWriter == null) {
                // Could happen asynchronously
                logger.debug("Ignoring sample from unknown writer {}", aSample.getWriterGuid());
                return null;
            }

            if (!checkOwnershipPolicy(matchedWriter, aSample)) {
                logger.debug("Ignoring sample from {}, since it is not a owner of instance", aSample.getWriterGuid());
                return null;
            }
        }

        // If SampleFilter is set and it rejects Sample, just return
        if (sampleFilter != null && !sampleFilter.acceptSample(aSample)) {
        	return null;
        }
        
        Duration lifespanDuration = getLifespan(aSample.getWriterGuid());
        if (!lifespanDuration.isInfinite()) {
            // NOTE, should we try to calculate timediff of source timestamp
            // and destination timestamp? And network delay? 
            // Since spec talks about adding duration to source timestamp. 
            // But allows using destination timestamp as well...
            watchdog.addTask(lifespanDuration.asMillis(), new Listener() {
                @Override
                public void triggerTimeMissed() {
                    logger.debug("Lifespan expired for {}", aSample);
                    clear(aSample);
                }
            });
        }

        return super.addSample(aSample);
    }

    
    
    // ----  ReaderCache implementation follows  -------------------------
    @Override
    public void changesBegin(int id) {
        logger.trace("changesBegin({})", id);
        List<Sample<T>> pendingSamples = new LinkedList<>();
        incomingSamples.put(id, pendingSamples);
    }

    @Override
    public void addChange(int id, Guid writerGuid, Data data, Time timestamp) {
    	if (filterSignature != null) { // If we have a filter...
    		ContentFilterInfo contentFilterInfo = data.getContentFilterInfo();
    		if (contentFilterInfo != null) { // ...and writer did filtering...
    			//...check, that our filter was applied or not
        		if (!contentFilterInfo.containsSignature(filterSignature)) {
        			logger.debug("Discarding data as writers ContentFilterInfo did not contain my filter signature");
        			return;
        		}
        	}
        }
        
        long sourceTimeStamp;
        if (timestamp != null) {
            sourceTimeStamp = timestamp.timeMillis();
        }
        else {
            sourceTimeStamp = System.currentTimeMillis();
        }

        long ts;
        if (destinationOrderKind == Kind.BY_RECEPTION_TIMESTAMP) {
            ts = System.currentTimeMillis();
        }
        else {
            ts = sourceTimeStamp; 
        }

        List<Sample<T>> coherentSet = getCoherentSet(writerGuid); // Get current CoherentSet for writer
        List<Sample<T>> pendingSamples = incomingSamples.get(id); 

        Sample<T> sample = new Sample<T>(writerGuid, marshaller, ++seqNum, ts, sourceTimeStamp, data);
        CoherentSet cs = sample.getCoherentSet();

        
        
        // Check, if we need to add existing CoherentSet into pendingSamples
        if (coherentSet.size() > 0) { // If no samples in cs, no need to add to pending samples 
            if (cs == null || 
                    cs.getStartSeqNum().getAsLong() == SequenceNumber.SEQUENCENUMBER_UNKNOWN.getAsLong() ||
                    cs.getStartSeqNum().getAsLong() != coherentSet.get(0).getCoherentSet().getStartSeqNum().getAsLong()) {
                // End of CoherentSet is detected, if CS attribute is missing, or it is SEQNUM_UNKNOWN,
                // or its startSeqNum is different
                pendingSamples.addAll(coherentSet);
                coherentSet.clear();
            }
        }

        if (data.dataFlag()) { // Add only Samples with contain Data
            if (cs != null) { // If we have a CS attribute, add it to coherentSet
                coherentSet.add(sample);
            }
            else {
                pendingSamples.add(sample);
            }
        }
        else {
            logger.debug("Skipping sample #{} from being delivered to reader, since it does not contain Data", data.getWriterSequenceNumber());
        }
    }

    private List<Sample<T>> getCoherentSet(Guid writerGuid) {
        List<Sample<T>> list = coherentSets.get(writerGuid);
        if (list == null) {
            list = new LinkedList<>();
            coherentSets.put(writerGuid, list);
        }

        return list;
    }

    @Override
    public void changesEnd(int id) {
        logger.trace("changesEnd({})", id);        

        List<Sample<T>> pendingSamples = incomingSamples.remove(id); 
        List<Sample<T>> acceptedSamples = new LinkedList<>();

        if (pendingSamples.size() > 0) {
            // Add each pending Sample to HistoryCache
            for (Sample<T> cc : pendingSamples) {
                if (addSample(cc) != null) {
                    acceptedSamples.add(cc);
                }
            }

            // Notify listeners 
            if (acceptedSamples.size() > 0) {
                for (SampleListener<T> aListener : listeners) {
                    aListener.onSamples(new LinkedList<>(pendingSamples)); // each Listener has its own List
                }
            }
        }
    }


    
    private boolean checkOwnershipPolicy(WriterProxy writer, Sample<T> sample) {
        if (!exclusiveOwnership) {
            return true;
        }

        Instance<T> inst = getOrCreateInstance(sample.getKey());

        return inst.claimOwnership(writer);
    }

    private Duration getLifespan(Guid writerGuid) {
        if (rtps_reader != null) {  // TODO: can be null on junit tests
            WriterProxy matchedWriter = rtps_reader.getMatchedWriter(writerGuid);
            if (matchedWriter != null) {
                QosLifespan lifespan = matchedWriter.getPublicationData().getQualityOfService().getLifespan();
                logger.trace("Setting lifespan of sample to {}", lifespan.getDuration());
                return lifespan.getDuration();
            }
            else {
                logger.warn("Matched writer was removed before Lifespan duration could be determined. Disabling Lifespan");
            }
        }

        return Duration.INFINITE;
    }

	public void setContentFilterSignature(byte[] signature) {
		this.filterSignature = signature;
	}
}
