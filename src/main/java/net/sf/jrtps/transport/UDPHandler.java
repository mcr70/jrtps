package net.sf.jrtps.transport;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.types.Locator;

/**
 * This Handler creaters receivers and writers for UDP protocol.
 * UDP is the only protocol that is required by the RTPS specification.
 *  
 * @author mcr70
 */
public class UDPHandler extends URIHandler {

    @Override
    Receiver createReceiver(Locator locator, BlockingQueue<byte[]> queue, int bufferSize) throws IOException {
        return new UDPReceiver(locator, queue, bufferSize);
    }

    @Override
    Writer createWriter(Locator locator, int bufferSize) throws IOException {
        return new UDPWriter(locator, bufferSize);
    }

}
