package net.sf.jrtps.udds;

import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;

class UDDSEntityFactory implements EntityFactory {

    @Override
    public <T> DataWriter<T> createDataWriter(Participant p, Class<T> type, RTPSWriter<T> rtpsWriter, 
            HistoryCache<T> hCache) {
        return new DataWriter<>(p, type, rtpsWriter, hCache);
    }

    @Override
    public <T> DataReader<T> createDataReader(Participant p, Class<T> type, RTPSReader<T> rtpsReader) {
        return new DataReader<>(p, type, rtpsReader);
    }
}
