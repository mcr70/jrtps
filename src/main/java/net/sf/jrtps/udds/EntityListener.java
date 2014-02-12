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
     * @param pd
     */
    public void participantDetected(ParticipantData pd);

    /**
     * Called when a previously know Participant has been lost. Participant gets
     * lost if it does not renew its lease in time.
     * 
     * @param pd
     */
    public void participantLost(ParticipantData pd);

    /**
     * Called when a liveliness of remote writer has been lost. This method is
     * will be called during writer liveliness protocol. See 8.4.13 Writer
     * Liveliness Protocol.
     * 
     * @param remoteWriter
     */
    public void livelinessLost(PublicationData remoteWriter);

    /**
     * Called when a liveliness of remote reader has been lost. Note, that there
     * is no mechanism built into protocol for the liveliness of the readers.
     * This method is called when jRTPS thinks remote reader is not reachable
     * anymore. This could happen for example, when reader does not respond to
     * writer in timely manner.
     * 
     * @param remoteReader
     */
    public void livelinessLost(SubscriptionData remoteReader);

    /**
     * Called when a new remote reader has been detected.
     * 
     * @param rd
     */
    public void readerDetected(SubscriptionData rd);

    /**
     * Called when a local writer is associated with remote reader
     * 
     * @param writer
     *            local writer
     * @param rd
     *            remote reader
     */
    public void readerMatched(DataWriter<?> writer, SubscriptionData rd);

    /**
     * Called when a new remote writer has been detected.
     * 
     * @param wd
     */
    public void writerDetected(PublicationData wd);

    /**
     * Called when a local reader is associated with remote writer
     * 
     * @param reader
     *            local reader
     * @param wd
     *            remote writer
     */
    public void writerMatched(DataReader<?> reader, PublicationData wd);

    /**
     * Called when a local writer cannot be associated with remote reader
     * because of inconsistent QoS.
     * 
     * @param writer
     *            local writer
     * @param rd
     *            remote reader
     */
    public void inconsistentQoS(DataWriter<?> writer, SubscriptionData rd);

    /**
     * Called when a local reader cannot be associated with remote writer
     * because of inconsistent QoS.
     * 
     * @param reader
     *            local reader
     * @param wd
     *            remote writer
     */
    public void inconsistentQoS(DataReader<?> reader, PublicationData wd);
}
