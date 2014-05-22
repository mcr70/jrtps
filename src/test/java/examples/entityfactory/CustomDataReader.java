package examples.entityfactory;

import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.Participant;

public class CustomDataReader<T> extends DataReader<T> {

    protected CustomDataReader(Participant p, Class<T> type, RTPSReader<T> reader) {
        super(p, type, reader);
    }

}
