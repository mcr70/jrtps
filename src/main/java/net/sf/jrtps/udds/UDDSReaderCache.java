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
import net.sf.jrtps.rtps.ReaderCache;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.types.Time;
import net.sf.jrtps.util.Watchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UDDSReaderCache<T> extends UDDSHistoryCache<T, PublicationData> implements ReaderCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(UDDSReaderCache.class);
    
    private Map<Guid, List<Sample<T>>> coherentSets = new HashMap<>(); // Used by reader
    private final Kind destinationOrderKind;
    private final Map<Integer, List<Sample<T>>> incomingSamples = new HashMap<>();
    
    UDDSReaderCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog) {
        super(eId, marshaller, qos, watchdog, true);
        
        destinationOrderKind = qos.getDestinationOrder().getKind();
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

        long ts = 0;
        if (destinationOrderKind == Kind.BY_RECEPTION_TIMESTAMP || timestamp == null) {
            ts = System.currentTimeMillis();
        }
        else {
            ts = timestamp.timeMillis(); 
        }

        List<Sample<T>> coherentSet = getCoherentSet(writerGuid); // Get current CoherentSet for writer
        List<Sample<T>> pendingSamples = incomingSamples.get(id); 

        Sample<T> sample = new Sample<T>(writerGuid, marshaller, ++seqNum, ts, data);
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
}
