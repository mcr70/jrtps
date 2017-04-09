package net.sf.jrtps.qos;

import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.EntityFactory;
import net.sf.jrtps.udds.HistoryCache;
import net.sf.jrtps.udds.Participant;

class TEntityFactory extends EntityFactory {
    @Override
    public <T> DataWriter<T> createDataWriter(Participant p, Class<T> type, String typeName, RTPSWriter<T> rtpsWriter,
            HistoryCache<T> hCache) {
        return new TDataWriter<>(p, type, rtpsWriter, hCache);
    }
}