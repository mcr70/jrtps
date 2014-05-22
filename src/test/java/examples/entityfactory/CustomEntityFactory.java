package examples.entityfactory;

import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.EntityFactory;
import net.sf.jrtps.udds.HistoryCache;
import net.sf.jrtps.udds.Participant;

public class CustomEntityFactory implements EntityFactory {

    @Override
    public <T> DataWriter<T> createDataWriter(Participant p, Class<T> type, RTPSWriter<T> rtpsWriter,
            HistoryCache<T> hCache) {
        return new CustomDataWriter<>(p, type, rtpsWriter, hCache);
    }

    @Override
    public <T> DataReader<T> createDataReader(Participant p, Class<T> type, RTPSReader<T> rtpsReader) {
        return new CustomDataReader<>(p, type, rtpsReader);
    }

}
