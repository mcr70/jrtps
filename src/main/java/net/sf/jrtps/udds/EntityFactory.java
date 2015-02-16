package net.sf.jrtps.udds;

import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;

/**
 * EntityFactory is used to create instances of uDDS entities. 
 * By giving an instance of EntityFactory to Participant, one
 * can provide customized entities to application.
 * 
 * @see Participant#Participant(int, int, EntityFactory, net.sf.jrtps.Configuration)
 * @see Participant#setEntityFactory(EntityFactory)
 * 
 * @author mcr70
 */
public class EntityFactory {

    /**
     * Empty constructor. 
     */
    protected EntityFactory() {
    }
    
    /**
     * Create a new DataWriter.
     * @param p Participant that is parent of created DataWriter
     * @param type a Class representing the type of writer
     * @param rtpsWriter RTPSWriter to be associated with created DataWriter
     * @param hCache HistoryCache of DataWriter
     * @return DataWriter
     */
    protected <T> DataWriter<T> createDataWriter(Participant p, Class<T> type, String typeName, RTPSWriter<T> rtpsWriter, 
            HistoryCache<T> hCache) {
        return new DataWriter<>(p, type, typeName, rtpsWriter, hCache);
    }
    
    /**
     * Create a new DataReader.
     * @param p Participant that is parent of created DataReader
     * @param type a Class representing the type of reader
     * @param rtpsReader RTPSReader to be associated with created DataReader
     * @return DataReader
     */
    protected <T> DataReader<T> createDataReader(Participant p, Class<T> type, String typeName, RTPSReader<T> rtpsReader) {
        return new DataReader<>(p, type, typeName, rtpsReader);
    }
}
