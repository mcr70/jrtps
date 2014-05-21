package net.sf.jrtps.udds;

import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;

/**
 * EntityFactory is used to create instances of uDDS entities. 
 * By giving an instance of EntityFactory to Participants constructor, one
 * can provide customized entities to application.
 * 
 * @see Participant#Participant(int, int, EntityFactory, net.sf.jrtps.Configuration)
 * @author mcr70
 */
public interface EntityFactory {
    /**
     * Create a new DataWriter.
     * @param p Participant that is parent of created DataWriter
     * @param type a Class representing the type of writer
     * @param rtpsWriter RTPSWriter to be associated with created DataWriter
     * @param hCache HistoryCache of DataWriter
     * @return DataWriter
     */
    <T> DataWriter<T> createDataWriter(Participant p, Class<T> type, RTPSWriter<T> rtpsWriter,
            HistoryCache<T> hCache);
    
    /**
     * Create a new DataReader.
     * @param p Participant that is parent of created DataReader
     * @param type a Class representing the type of reader
     * @param rtpsReader RTPSReader to be associated with created DataReader
     * @return DataReader
     */
    <T> DataReader<T> createDataReader(Participant p, Class<T> type, RTPSReader<T> rtpsReader);
}
