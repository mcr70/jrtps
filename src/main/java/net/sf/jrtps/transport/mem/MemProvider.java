package net.sf.jrtps.transport.mem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
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

public class MemProvider extends TransportProvider {

    static final int LOCATOR_KIND_MEM = 0x8001;
    
    private static final Map<Locator, BlockingQueue<byte[]>> queues = new HashMap<>();
    private static final Map<URI, Locator> locators = new HashMap<>();
    
    public MemProvider(Configuration config) {
        super(config);
    }

    @Override
    public Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery,
            BlockingQueue<byte[]> queue, int bufferSize) throws IOException {
        uri.getHost();
        BlockingQueue<byte[]> inQueue = getQueue(uri);
        
        return new MemReceiver(participantId, inQueue, queue);
    }

    private synchronized BlockingQueue<byte[]> getQueue(URI uri) throws UnknownHostException {
        BlockingQueue<byte[]> q = queues.get(uri);
        if (q == null) {
            InetAddress ia = InetAddress.getByName(uri.getHost());
            Locator loc = new Locator(ia, uri.getPort());
            
            q = new ArrayBlockingQueue<>(128); // TODO: hardcoded
            queues.put(loc, q);
        }
        
        return q;
    }

    @Override
    public Transmitter createTransmitter(Locator locator, int bufferSize) throws IOException {
        BlockingQueue<byte[]> outQueue = queues.get(locator);
        return new MemTransmitter(outQueue, bufferSize);
    }

    @Override
    public Locator getDefaultDiscoveryLocator(int domainId) {
        byte[] addr = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 239, (byte) 255, 0, 1 };
        PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
        
        return new Locator(LOCATOR_KIND_MEM, pnp.getPortBase() + pnp.getDomainIdGain() * domainId + pnp.getD0(), addr);
    }
}
