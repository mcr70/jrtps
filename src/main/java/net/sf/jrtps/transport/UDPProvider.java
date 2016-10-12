package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Provider creates receivers and writers for UDP protocol.
 * UDP is the only protocol that is required by the RTPS specification.
 * Both unicast and multicast is supported.
 *  
 * @author mcr70
 */
public class UDPProvider extends TransportProvider {
    private static final Logger logger = LoggerFactory.getLogger(UDPProvider.class);   

    private HashMap<Locator, UDPTransmitter> transmitters = new HashMap<>();
    
    /**
     * Provider scheme, that is used in configuring UDP TranportProvider URIs.
     */
    public static final String PROVIDER_SCHEME = "udp";

	private int participantId;

    public UDPProvider(Configuration config, int participantId) {
        super(config);
		this.participantId = participantId;
    }

    @Override
    public Receiver getReceiver(Locator locator, BlockingQueue<byte[]> queue) throws IOException {
    	UDPLocator loc = (UDPLocator) locator;
        ReceiverConfig rConfig = getDatagramSocket(loc);

        return new UDPReceiver(loc, rConfig, queue, getConfiguration().getBufferSize());
    }

    @Override
    public Transmitter getTransmitter(Locator locator) throws IOException {
    	UDPTransmitter tr = transmitters.get(locator);
    	if (tr == null) {
    		tr = new UDPTransmitter(new UDPLocator(locator), getConfiguration().getBufferSize());
    		transmitters.put(locator, tr);
    	}
    	return tr;
    }

    @Override
    public Locator createLocator(URI uri, int domainId, int participantId, boolean isDiscovery) {
        byte[] address = new byte[16];
        try {
            InetAddress addr = InetAddress.getByName(uri.getHost());
            byte[] bytes = addr.getAddress();

            int kind;
            if (bytes.length == 4) {
                kind = Locator.LOCATOR_KIND_UDPv4;

                address[12] = bytes[0];
                address[13] = bytes[1];
                address[14] = bytes[2];
                address[15] = bytes[3];
            } 
            else {
                kind = Locator.LOCATOR_KIND_UDPv6;
                address = bytes;
            }
            int port = uri.getPort();
            
            if (port == -1) { // Port number is determined with PortNumberParameters
                PortNumberParameters pnp = getConfiguration().getPortNumberParameters();            		
            	if (isDiscovery) { // Determine discovery port
            		if (addr.isMulticastAddress()) {
            			port = pnp.getDiscoveryMulticastPort(domainId);
            		}
            		else {
            			port = pnp.getDiscoveryUnicastPort(domainId, participantId);
            		}
            	}
            	else { // Determine user data port 
            		if (addr.isMulticastAddress()) {
            			port = pnp.getUserdataMulticastPort(domainId);
            		}
            		else {
            			port = pnp.getUserdataUnicastPort(domainId, participantId);
            		}
            	}
            }
            
            return new UDPLocator(uri, kind, port, address);
        } catch (UnknownHostException e) {
            logger.warn("Failed to create Locator", e);
        }

        return null;
    }

    @Override
    public void close() {
    	for (UDPTransmitter tr : transmitters.values()) {
    		tr.close();
    	}
    }


    private ReceiverConfig getDatagramSocket(UDPLocator locator) throws IOException {
	    logger.trace("Creating DatagramSocket for URI {}", locator.getUri());
	
	    InetAddress ia = InetAddress.getByName(locator.getUri().getHost());
	    DatagramSocket ds = null;
	    int port = locator.getPort();
	
	    boolean participantIdFixed = participantId != -1;
		
	    if (ia.isMulticastAddress()) {	
	        ds = new MulticastSocket(port);
	        ((MulticastSocket) ds).joinGroup(ia);
	    }
	    else {
	        int pId = participantIdFixed ? participantId : 0;
	        PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
	        
	        do {
	            logger.trace("Trying port {}", port);
	
	            try {
	                ds = new DatagramSocket(port);
	                logger.trace("Port set to {}", port);
	                participantId = pId;
	                break;
	            }
	            catch(SocketException se) {
	                pId++;
	                port++;
	            }
	        }
	        while(ds == null && !participantIdFixed && pId < pnp.getDomainIdGain() + pnp.getD3());
	    }
	
	    if (ds == null) {
	    	throw new RuntimeException("Failed to get DatagramSocket for " + locator.getUri() +
	    			", participantId " + participantId);
	    }
	    
	    return new ReceiverConfig(participantId, ds);
	}
}
