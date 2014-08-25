package net.sf.jrtps.rtps;

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
    
    /**
     * This method gets called when liveliness is first lost, and then
     * restored again.
     * @param pd
     */
    void livelinessRestored(PublicationData pd);    
}
