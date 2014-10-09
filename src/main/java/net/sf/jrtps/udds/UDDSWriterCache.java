package net.sf.jrtps.udds;


import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterCache;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.util.Watchdog;
import net.sf.jrtps.util.Watchdog.Listener;

class UDDSWriterCache<T> extends UDDSHistoryCache<T, SubscriptionData> implements WriterCache<T> {    
    private long lifeSpanDuration;
    
    UDDSWriterCache(EntityId eId, Marshaller<T> marshaller, QualityOfService qos, Watchdog watchdog) {
        super(eId, marshaller, qos, watchdog, false);

        lifeSpanDuration = qos.getLifespan().getDuration().asMillis();
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



    @Override
    public void addSample(final Sample<T> aSample) {
        if (lifeSpanDuration > 0) {
            // NOTE, should we try to calculate timediff of source timestamp
            // and destination timestamp? And network delay? 
            // Since spec talks about adding duration to source timestamp. 
            // But allows using destination timestamp as well...
            watchdog.addTask(lifeSpanDuration, new Listener() {
                @Override
                public void triggerTimeMissed() {
                    clear(aSample);
                }
            });
        }
        
        super.addSample(aSample);
    }
}
