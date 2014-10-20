package net.sf.jrtps.qos;

import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.HistoryCache;
import net.sf.jrtps.udds.Participant;

class TDataWriter<T> extends DataWriter<T> {
    TDataWriter(Participant p, Class<T> type, RTPSWriter<T> writer, HistoryCache<T> hCache) {
        super(p, type, writer, hCache);
    }

    public void write(T sample, long timestamp) {
        try {
            hCache.write(sample, timestamp); // Write a sample with given timestamp
        } finally {
            super.notifyReaders(); // notify remote readers of new data in HistoryCache
        }
    }
}