package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;

/**
 * EntityListener.
 * 
 * @author mcr70
 */
public interface EntityListener {
    /**
     * Called when a new Participant has been detected.
     * 
     * @param pd ParticipantData
     */
    public void participantDetected(ParticipantData pd);

    /**
     * Called when a previously know Participant has been lost. Participant gets
     * lost if it does not renew its lease in time.
     * 
     * @param pd ParticipantData
     */
    public void participantLost(ParticipantData pd);

    /**
     * Called when a liveliness of remote writer has been lost. This method is
     * will be called during writer liveliness protocol. See 8.4.13 Writer
     * Liveliness Protocol.
     * 
     * @param remoteWriter
     */
    //public void livelinessLost(PublicationData remoteWriter);

    /**
     * Called when a liveliness of remote reader has been lost. Note, that there
     * is no mechanism built into protocol for the liveliness of the readers.
     * This method is called when jRTPS thinks remote reader is not reachable
     * anymore. This could happen for example, when reader does not respond to
     * writer in timely manner.
     * 
     * @param remoteReader
     */
    //public void livelinessLost(SubscriptionData remoteReader);

    /**
     * Called when a new remote reader has been detected.
     * 
     * @param rd SubscriptionData of the reader that was detected
     */
    public void readerDetected(SubscriptionData rd);

    /**
     * Called when a new remote writer has been detected.
     * 
     * @param wd PublicationData of the writer that was detected
     */
    public void writerDetected(PublicationData wd);
}
