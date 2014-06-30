package examples.entityfactory;

import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.HistoryCache;
import net.sf.jrtps.udds.Participant;

public class CustomDataWriter<T> extends DataWriter<T> {
    CustomDataWriter(Participant p, Class<T> type, RTPSWriter<T> writer, HistoryCache<T> hCache) {
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
