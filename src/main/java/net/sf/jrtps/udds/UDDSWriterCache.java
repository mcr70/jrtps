package net.sf.jrtps.udds;


import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.rtps.WriterCache;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.util.Watchdog;

class UDDSWriterCache<T> extends UDDSHistoryCache<T, SubscriptionData> implements WriterCache<T> {    
    UDDSWriterCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog) {
        super(eId, marshaller, qos, watchdog);
    }

    // ----  WriterCache implementation follows  -------------------------
    /**
     * Gets the smallest sequence number this HistoryCache has.
     * 
     * @return seqNumMin
     */
    @Override
    public long getSeqNumMin() {
        long seqNumMin = 0;
        synchronized (samples) {
            if (samples.size() > 0) {
                seqNumMin = samples.first().getSequenceNumber();
            }
        }

        return seqNumMin;
    }

    /**
     * Gets the biggest sequence number this HistoryCache has.
     * 
     * @return seqNumMax
     */
    @Override
    public long getSeqNumMax() {
        long seqNumMax = 0;

        synchronized (samples) {
            if (samples.size() > 0) {
                seqNumMax = samples.last().getSequenceNumber();
            }
        }

        return seqNumMax;
    }
}
