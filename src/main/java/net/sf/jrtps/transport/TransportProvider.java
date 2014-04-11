package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for creating receivers and writers for communication.
 * 
 * @author mcr70
 */
public abstract class TransportProvider {
    private static final Logger log = LoggerFactory.getLogger(TransportProvider.class);
    
    private static HashMap<String, TransportProvider> providersForScheme = new HashMap<>();
    private static HashMap<Integer, TransportProvider> providersForKind = new HashMap<>();
    
    private Configuration config;
   

    protected TransportProvider(Configuration config) {
        this.config = config;
    }
    
    protected Configuration getConfiguration() {
        return config;
    }
    
    /**
     * get a provider for given scheme. Scheme is the same, as is used by
     * java.net.URI class. A TransportProvider need to be first registered with
     * registerProvider.
     * 
     * @param scheme scheme
     * @return TransportProvider, or null if there was not TransportProvider registered with given scheme
     */
    public static TransportProvider getInstance(String scheme) {
        return providersForScheme.get(scheme);
    }
    
    /**
     * Get a provider for given Locator kind. Remote entities advertise Locators, that can be used to
     * connect to them. 
     * 
     * @param locator
     * @return TransportProvider, or null if there was not TransportProvider registered with given Locator.kind
     * @see Locator#getKind()
     */
    public static TransportProvider getInstance(Locator locator) {
        return providersForKind.get(locator.getKind());
    }

    /**
     * Registers a TranportProvider with given scheme and kind
     * 
     * @param scheme Scheme for the provider
     * @param provider TranportProvider to register
     * @param kinds Kinds of the Locators, that will be matched to given provider
     */
    public static void registerTransportProvider(String scheme, TransportProvider provider, int ... kinds) {
        log.debug("Registering provider for scheme '{}', kind {}: {}", scheme, kinds, provider);
        providersForScheme.put(scheme, provider);
        
        for (int kind : kinds) {
            providersForKind.put(kind, provider);
        }
    }
    
    /**
     * Creates a new Receiver. If the URI has a port defined, it will be used. If not, domainId, participantId and
     * discovery parameters are used to create port number according to specification.
     * 
     * @param uri Uri of the receiver to create.
     * @param domainId domainId
     * @param participantId participantId
     * @param discovery set to true, if the receiver created will be for discovery
     * @param queue a BlockingQueue, that should be populated with byte[] received by the Receiver.
     * @param bufferSize Size of the buffer that should be used during reception.
     * @return Receiver, or null if Receiver could not be created
     * @throws IOException 
     */
    public abstract Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue, int bufferSize) throws IOException;

    /**
     * Creates a new Writer. Remote entities advertise how they can be reached by the means of Locator.
     * 
     * @param locator Locator used
     * @param bufferSize Size of the buffer, that should be used byt the Writer
     * @return Writer
     * @throws IOException
     */
    public abstract Writer createWriter(Locator locator, int bufferSize) throws IOException;
}
