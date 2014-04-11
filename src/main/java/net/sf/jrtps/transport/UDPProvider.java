package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Provider creates receivers and writers for UDP protocol.
 * UDP is the only protocol that is required by the RTPS specification.
 *  
 * @author mcr70
 */
public class UDPProvider extends TransportProvider {
    private static final Logger log = LoggerFactory.getLogger(UDPProvider.class);
    
    public UDPProvider(Configuration config) {
        super(config);
    }
    
    @Override
    public Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue, int bufferSize) throws IOException {
        DatagramSocket datagramSocket = getDatagramSocket(uri, domainId, participantId, 
                getConfiguration().getPortNumberParameters(), discovery);
        
        return new UDPReceiver(uri, datagramSocket, queue, bufferSize);
    }

    @Override
    public Writer createWriter(Locator locator, int bufferSize) throws IOException {
        return new UDPWriter(locator, bufferSize);
    }

    private DatagramSocket getDatagramSocket(URI uri, int domainId, int participantId, PortNumberParameters pnp, boolean discovery) throws IOException {
        InetAddress ia = InetAddress.getByName(uri.getHost());
        DatagramSocket ds = null;
        int port = uri.getPort();
        
        boolean participantIdFixed = participantId != -1;
        
        if (port == -1) {
            log.trace("Port number is not specified in URI {}, using {}", uri, pnp);
        }
        
        if (ia.isMulticastAddress()) {
            if (port == -1) {
                port = discovery ? pnp.getDiscoveryMulticastPort(domainId) : pnp.getUserdataMulticastPort(domainId);
            }
            
            ds = new MulticastSocket(port);
            ((MulticastSocket) ds).joinGroup(ia);
        }
        else {
            int pId = participantIdFixed ? participantId : 0;
            boolean portFound = port != -1;
    
            do {
                if (!portFound) {
                    port = discovery ? pnp.getDiscoveryUnicastPort(domainId, pId) : pnp.getUserdataUnicastPort(domainId, pId);
                }

                try {
                    ds = new DatagramSocket(port);
                    log.trace("Port set to {}", port);
                    break;
                }
                catch(SocketException se) {
                    pId++;
                }
            }
            while(ds == null && !participantIdFixed && pId < pnp.getDomainIdGain() + pnp.getD3());
        }
        
        return ds;
    }
}
