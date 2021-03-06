package net.sf.jrtps.rtps;

import net.sf.jrtps.message.Data;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Time;


/**
 * ReaderCache represents history cache from the RTPSReader point of view.
 * RTPSReader uses ReaderCache to pass samples coming from network to DDS layer. 
 * 
 * @author mcr70
 *
 * @param <T> Type of the ReaderCache 
 */
public interface ReaderCache<T> {
    /**
     * Notifies implementing class that a new set of changes is coming from RTPS layer.
     * 
     * @param id Id of the message that caused this invocation
     */
    void changesBegin(int id); 
    
    /**
     * Adds a new change to ReaderCache. It is the responsibility of the implementing class
     * to decide whether or not this Sample is actually made available to applications or not.
     * 
     * @param id id
     * @param writerGuid Guid of the writer
     * @param data Data
     * @param timestamp Time
     */
    void addChange(int id, Guid writerGuid, Data data, Time timestamp);
    
    /**
     * Notifies implementing class that there are no more samples coming from the RTPS layer.
     * I.e. the whole RTPS message has been processed.
     *
     * @param id Id of the message that caused this invocation
     */
    void changesEnd(int id);    
}
