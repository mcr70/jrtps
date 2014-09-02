package net.sf.jrtps.transport.mem;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.transport.PortNumberParameters;
import net.sf.jrtps.transport.Receiver;
import net.sf.jrtps.transport.Transmitter;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemProvider extends TransportProvider {
    private static final Logger logger = LoggerFactory.getLogger(MemProvider.class);
    static final int LOCATOR_KIND_MEM = 0x8001;
    
    private static final Map<Locator, BlockingQueue<byte[]>> queues = new HashMap<>();
    
    public MemProvider(Configuration config) {
        super(config);
    }

    @Override
    public Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery,
            BlockingQueue<byte[]> queue, int bufferSize) throws IOException {        
        BlockingQueue<byte[]> inQueue = getQueue(new MemLocator(uri));
        
        return new MemReceiver(new MemLocator(uri), participantId, inQueue, queue);
    }

    @Override
    public Transmitter createTransmitter(Locator locator, int bufferSize) throws IOException {
        BlockingQueue<byte[]> outQueue = getQueue(locator);
        logger.debug("Creating transmitter for {}, queue: {}", locator, outQueue.hashCode());
        return new MemTransmitter(outQueue, bufferSize);
    }


    @Override
    public Locator getDefaultDiscoveryLocator(int domainId) {
        byte[] addr = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 239, (byte) 255, 0, 1 };
        PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
        
        return new Locator(LOCATOR_KIND_MEM, pnp.getPortBase() + pnp.getDomainIdGain() * domainId + pnp.getD0(), addr);
    }



    private synchronized BlockingQueue<byte[]> getQueue(Locator loc) {
        BlockingQueue<byte[]> q = queues.get(loc);
        if (q == null) {
            q = new ArrayBlockingQueue<>(128); // TODO: hardcoded
            logger.debug("Adding new queue for {}: {}", loc, q.hashCode());
            
            queues.put(loc, q);
        }
        
        return q;
    }

    
    @Override
    public Locator createDiscoveryLocator(URI uri, int domainId) {
        if (uri.getPort() == -1) {
            PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
            
            return new MemLocator(uri, pnp.getDiscoveryMulticastPort(domainId));
        }
        
        return new MemLocator(uri);
    }
}
