package net.sf.jrtps.transport;

import java.net.DatagramSocket;

/**
 * Package access. This class represents a struct, that holds UDP DatagramSocket created, 
 * along with participantId which may have been generated during creation. 
 * 
 * @author mcr70
 */
class ReceiverConfig {
    final DatagramSocket ds;
    final int participantId;
    final boolean discovery;

    public ReceiverConfig(int participantId, DatagramSocket ds, boolean discovery) {
        this.participantId = participantId;
        this.ds = ds;
        this.discovery = discovery;
    }
}