package net.sf.jrtps.transport;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.types.Locator;

/**
 * An abstract class for creating receivers and writers for communication.
 * 
 * @author mcr70
 */
abstract class URIHandler {
    private static HashMap<String, URIHandler> handlers = new HashMap<>();
   
    /**
     * get a handler for given scheme. Scheme is the same, as is used by
     * java.net.URI class. An URIHandler need to be first registered with
     * registerURIHandler.
     * 
     * @param scheme scheme
     * @return URIHandler, or null if there was not URIHandler registered with given scheme
     */
    static URIHandler getInstance(String scheme) {
        return handlers.get(scheme);
    }
    
    /**
     * Registers an URIHandler with given scheme.
     * 
     * @param scheme Scheme for the URIHandler
     * @param handler URIHandler
     */
    static void registerURIHandler(String scheme, URIHandler handler) {
        handlers.put(scheme, handler);
    }
    
    /**
     * Creates a new Receiver.
     * @param locator
     * @param queue
     * @param bufferSize
     * @return Receiver
     * @throws IOException
     */
    abstract Receiver createReceiver(Locator locator, BlockingQueue<byte[]> queue, int bufferSize) throws IOException;

    /**
     * Creates a new Writer.
     * @param locator
     * @param bufferSize
     * @return Writer
     * @throws IOException
     */
    abstract Writer createWriter(Locator locator, int bufferSize) throws IOException;
}
