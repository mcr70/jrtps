package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.PublicationData;

/**
 * WriterListener can be added to DataReader. This can be used to track remote writers 
 * when they can / cannot be matched with local DataReader.
 * 
 * @author mcr70
 * @see DataReader#addWriterListener(WriterListener)
 */
public interface WriterListener {
    /**
     * This method is called when a remote writer has been successfully matched. 
     * @param sd PublicationData of the remote writer
     */
    void writerMatched(PublicationData sd);

    /**
     * This method is called when a remote writer cannot be matched with DataReader due to 
     * inconsistent QualityOfService. 
     * @param sd PublicationData of the remote writer
     */
    void inconsistentQoS(PublicationData sd);
}
