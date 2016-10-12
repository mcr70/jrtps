package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Provider creates receivers and writers for memory based transport protocol.
 *  
 * @author mcr70
 */
public class MemProvider extends TransportProvider {
	private static final Logger logger = LoggerFactory.getLogger(MemProvider.class);
	public static final int LOCATOR_KIND_MEM = 0x8001;

	private static final Map<Locator, BlockingQueue<byte[]>> queues = new HashMap<>();

	public MemProvider(Configuration config) {
		super(config);
	}

	@Override
	public Receiver getReceiver(URI uri, int domainId, int participantId, boolean discovery,
			BlockingQueue<byte[]> queue) throws IOException {        
		BlockingQueue<byte[]> inQueue = getQueue(new MemLocator(uri));

		return new MemReceiver(new MemLocator(uri), participantId, inQueue, queue);
	}

	@Override
	public Transmitter getTransmitter(Locator locator) throws IOException {
		BlockingQueue<byte[]> outQueue = getQueue(locator);
		logger.debug("Creating transmitter for {}, queue: {}", locator, outQueue.hashCode());
		return new MemTransmitter(outQueue, getConfiguration().getBufferSize());
	}

	@Override
	public Locator createLocator(URI uri, int domainId, int participantId, boolean isDiscovery) {
		if (uri.getPort() == -1) {
			PortNumberParameters pnp = getConfiguration().getPortNumberParameters();
			int port;
			if (isDiscovery) {
				return new MemLocator(uri, pnp.getDiscoveryMulticastPort(domainId));
			}
			else {
				return new MemLocator(uri, pnp.getUserdataMulticastPort(domainId));
			}
		}	

		return new MemLocator(uri);
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
}
