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

    public ReceiverConfig(int participantId, DatagramSocket ds) {
        this.participantId = participantId;
        this.ds = ds;
    }
}