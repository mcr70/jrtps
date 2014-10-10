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
import net.sf.jrtps.message.parameter.QosDestinationOrder.Kind;
import net.sf.jrtps.message.parameter.QosLifespan;
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

    private RTPSReader<T> rtps_reader;

    UDDSReaderCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog) {
        super(eId, marshaller, qos, watchdog, true);
        
        destinationOrderKind = qos.getDestinationOrder().getKind();
    }

    /**
     * Sets RTPSReader associated with this cache. RTPSReader is used to
     *  
     * @param rtps_reader
     */
    void setRTPSReader(RTPSReader<T> rtps_reader) {
        this.rtps_reader = rtps_reader;
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

        if (pendingSamples.size() > 0) {
            // Add each pending Sample to HistoryCache
            for (Sample<T> cc : pendingSamples) {
                addSample(cc);
            }

            // Notify listeners 
            for (SampleListener<T> aListener : listeners) {
                aListener.onSamples(new LinkedList<>(pendingSamples)); // each Listener has its own List
            }
        }
    }


    @Override
    public void addSample(final Sample<T> aSample) {
        Duration lifespanDuration = getLifespan(aSample.getWriterGuid());
        if (!Duration.INFINITE.equals(lifespanDuration)) {
            // NOTE, should we try to calculate timediff of source timestamp
            // and destination timestamp? And network delay? 
            // Since spec talks about adding duration to source timestamp. 
            // But allows using destination timestamp as well...
            watchdog.addTask(lifespanDuration.asMillis(), new Listener() {
                @Override
                public void triggerTimeMissed() {
                    clear(aSample);
                }
            });
        }
        
        super.addSample(aSample);
    }

    private Duration getLifespan(Guid writerGuid) {
        WriterProxy matchedWriter = rtps_reader.getMatchedWriter(writerGuid);
        if (matchedWriter != null) {
            QosLifespan lifespan = matchedWriter.getPublicationData().getQualityOfService().getLifespan();
            return lifespan.getDuration();
        }
        else {
            logger.warn("Matched writer was removed before Lifespan duration could be determined. Disabling Lifespan");
        }

        return Duration.INFINITE;
    }
}
