package net.sf.jrtps;

import net.sf.jrtps.builtin.PublicationData;

/**
 * A Listener for writer liveliness protocol.
 * @author mcr70
 */
public interface WriterLivelinessListener {
    /**
     * This method is called when writer liveliness is lost.
     * @param pd
     */
    void livelinessLost(PublicationData pd);
}
