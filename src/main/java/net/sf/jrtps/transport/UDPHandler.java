package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Handler creaters receivers and writers for UDP protocol.
 * UDP is the only protocol that is required by the RTPS specification.
 *  
 * @author mcr70
 */
public class UDPHandler extends URIHandler {
    private static final Logger log = LoggerFactory.getLogger(UDPHandler.class);
    
    public UDPHandler(Configuration config) {
        super(config);
    }
    
    @Override
    public Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue, int bufferSize) throws IOException {
        DatagramSocket datagramSocket = getDatagramSocket(uri, domainId, participantId, 
                getConfiguration().getPortNumberParameters(), discovery);
        
        return new UDPReceiver(datagramSocket, queue, bufferSize);
    }

    @Override
    public Writer createWriter(URI uri, int domainId, int participantId, int bufferSize) throws IOException {
        return null; //return new UDPWriter(locator, bufferSize);
    }

    private DatagramSocket getDatagramSocket(URI uri, int domainId, int participantId, PortNumberParameters pnp, boolean discovery) throws IOException {
        InetAddress ia = InetAddress.getByName(uri.getHost());
        DatagramSocket ds = null;
        int port = uri.getPort();
        
        if (ia.isMulticastAddress()) {
            if (port == -1) {
                port = discovery ? pnp.getDiscoveryMulticastPort(domainId) : pnp.getUserdataMulticastPort(domainId);
            }
            
            ds = new MulticastSocket(port);
            ((MulticastSocket) ds).joinGroup(ia);
        }
        else {
            int pId = participantId;
            boolean portFound = port != -1;
    
            do {
                if (!portFound) {
                    port = discovery ? pnp.getDiscoveryUnicastPort(domainId, pId) : pnp.getUserdataUnicastPort(domainId, pId);
                }
                log.debug("Trying port {}", port);
                try {
                    ds = new DatagramSocket(port);
                }
                catch(SocketException se) {
                    pId++;
                }
            }
            while(ds == null && !portFound);
            
            participantId = pId;
            log.debug("participantId set to {}", participantId);
        }
        
        return ds;
    }
}
