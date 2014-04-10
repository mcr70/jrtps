package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for creating receivers and writers for communication.
 * 
 * @author mcr70
 */
public abstract class URIHandler {
    private static final Logger log = LoggerFactory.getLogger(URIHandler.class);
    
    private static HashMap<String, URIHandler> handlers = new HashMap<>();
    private Configuration config;
   

    protected URIHandler(Configuration config) {
        this.config = config;
    }
    
    protected Configuration getConfiguration() {
        return config;
    }
    
    /**
     * get a handler for given scheme. Scheme is the same, as is used by
     * java.net.URI class. An URIHandler need to be first registered with
     * registerURIHandler.
     * 
     * @param scheme scheme
     * @return URIHandler, or null if there was not URIHandler registered with given scheme
     */
    public static URIHandler getInstance(String scheme) {
        return handlers.get(scheme);
    }
    
    /**
     * Registers an URIHandler with given scheme.
     * 
     * @param scheme Scheme for the URIHandler
     * @param handler URIHandler
     */
    public static void registerURIHandler(String scheme, URIHandler handler) {
        log.debug("Registering URI handler for scheme '{}': {}", scheme, handler);
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
    public abstract Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue, int bufferSize) throws IOException;

    /**
     * Creates a new Writer.
     * @param locator
     * @param bufferSize
     * @return Writer
     * @throws IOException
     */
    public abstract Writer createWriter(URI uri, int domainId, int participantId, int bufferSize) throws IOException;
}
