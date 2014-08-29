package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
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

    /**
     * Provider scheme, that is used in configuring UDP TranportProvider URIs.
     */
    public static final String PROVIDER_SCHEME = "udp";

    public UDPProvider(Configuration config) {
        super(config);
    }

    @Override
    public Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue, int bufferSize) throws IOException {

        ReceiverConfig rConfig = getDatagramSocket(uri, domainId, participantId, 
                getConfiguration().getPortNumberParameters(), discovery);

        return new UDPReceiver(uri, rConfig, queue, bufferSize);
    }

    @Override
    public Transmitter createTransmitter(Locator locator, int bufferSize) throws IOException {
        return new UDPTransmitter(locator, bufferSize);
    }

    private ReceiverConfig getDatagramSocket(URI uri, int domainId, int participantId, PortNumberParameters pnp, boolean discovery) throws IOException {
        logger.trace("Creating DatagramSocket for URI {}, domain {}, pId {}", uri, domainId, participantId);

        InetAddress ia = InetAddress.getByName(uri.getHost());
        DatagramSocket ds = null;
        int port = uri.getPort();

        boolean participantIdFixed = participantId != -1;

        if (port == -1) {
            logger.trace("Port number is not specified in URI {}, using {}", uri, pnp);
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
                logger.trace("Trying pId {}", pId);
                if (!portFound) {
                    port = discovery ? pnp.getDiscoveryUnicastPort(domainId, pId) : pnp.getUserdataUnicastPort(domainId, pId);
                }

                try {
                    ds = new DatagramSocket(port);
                    logger.trace("Port set to {}", port);
                    break;
                }
                catch(SocketException se) {
                    pId++;
                }
            }
            while(ds == null && !participantIdFixed && pId < pnp.getDomainIdGain() + pnp.getD3());
            participantId = pId;
        }

        return new ReceiverConfig(participantId, ds, discovery);
    }

    @Override
    public Locator getDefaultDiscoveryLocator(int domainId) {
        byte[] addr = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 239, (byte) 255, 0, 1 };
        PortNumberParameters pnp = getConfiguration().getPortNumberParameters();

        return new Locator(Locator.LOCATOR_KIND_UDPv4, pnp.getPortBase() + pnp.getDomainIdGain() * domainId + pnp.getD0(), addr);
    }

    @Override
    public Locator createDiscoveryLocator(URI uri, int domainId) {
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
            if (port == -1) {
                PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
                port = pnp.getDiscoveryMulticastPort(domainId);
            }
            
            return new Locator(kind, port, address);
        } catch (UnknownHostException e) {
            logger.warn("Failed to create Locator", e);
        }

        return null;
    }
}
