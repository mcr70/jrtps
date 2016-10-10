package net.sf.jrtps.transport;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for creating receivers and transmitters for communication.
 * 
 * @author mcr70
 */
public abstract class TransportProvider {
    private static final Logger logger = LoggerFactory.getLogger(TransportProvider.class);

    private static HashMap<String, TransportProvider> providersForScheme = new HashMap<>();
    private static HashMap<Integer, TransportProvider> providersForKind = new HashMap<>();
    
    private Configuration config;

    /**
     * Constructor for TransportProvider.
     * @param config Configuration
     */
    protected TransportProvider(Configuration config) {
        this.config = config;
    }

    /**
     * Gets the configuration
     * @return Configuration
     */
    protected Configuration getConfiguration() {
        return config;
    }

    /**
     * Gets the port number parameters from configuration.
     * @return PortNumberParameters
     */
    protected PortNumberParameters getPortNumberParameters() {
        return config.getPortNumberParameters();
    }
    
    /**
     * Get a TranportProvider for given scheme. Scheme is the same, as is used by
     * java.net.URI class. A TransportProvider need to be first registered with
     * registerProvider.
     * 
     * @param scheme scheme
     * @return TransportProvider, or null if there was not TransportProvider registered with given scheme
     * @see java.net.URI#getScheme()
     */
    public static TransportProvider getInstance(String scheme) {
        return providersForScheme.get(scheme);
    }

    /**
     * Get a provider for given Locator kind. Remote entities advertise Locators, that can be used to
     * connect to them. This method is used to get a corresponding TransportProvider if one exists.
     * 
     * @param locator Locator
     * @return TransportProvider, or null if there was not TransportProvider registered with given Locator.kind
     */
    public static TransportProvider getInstance(Locator locator) {
        TransportProvider transportProvider = providersForKind.get(locator.getKind());
        if (transportProvider == null) {
            logger.warn("Could not get TransportProvider for Locator kind {}", locator.getKind());
        }
        
        return transportProvider;
    }
    
    
    /**
     * Registers a TranportProvider with given scheme and kind
     * 
     * @param scheme Scheme for the provider
     * @param provider TranportProvider to register
     * @param kinds Kinds of the Locators, that will be matched to given provider
     */
    public static void registerTransportProvider(String scheme, TransportProvider provider, int ... kinds) {
        logger.debug("Registering provider for scheme '{}', kinds {}: {}", scheme, kinds, provider);
        providersForScheme.put(scheme, provider);

        for (int kind : kinds) {
            providersForKind.put(kind, provider);
        }
    }

    
    /**
     * Gets all the registered TransportProviders.
     * @return A Collection of TransportProviders
     */
    public static Collection<TransportProvider> getTransportProviders() {
        return providersForScheme.values();
    }
    
    /**
     * Creates a new Receiver. If the URI has a port defined, it should be used. If not, domainId, participantId and
     * discovery parameters should be used to create port number according to specification.
     * 
     * @param uri Uri of the receiver to create.
     * @param domainId domainId
     * @param participantId participantId
     * @param discovery set to true, if the receiver created will be for discovery
     * @param queue a BlockingQueue, that should be populated with byte[] received by the Receiver.
     * @return Receiver, or null if Receiver could not be created
     * @throws IOException on IOException
     * 
     * @see #getPortNumberParameters()
     */
    public abstract Receiver createReceiver(URI uri, int domainId, int participantId, boolean discovery, BlockingQueue<byte[]> queue) throws IOException;

    /**
     * Gets a Transmitter. Remote entities advertise how they can be reached by the means of Locator.
     * 
     * @param locator Locator used
     * @return Writer
     * @throws IOException on IOException
     */
    public abstract Transmitter getTransmitter(Locator locator) throws IOException;
    
    /**
     * Get the default Locator for discovery.
     * @param domainId Domain ID
     * @return Locator
     */
    public abstract Locator getDefaultDiscoveryLocator(int domainId);

    /**
     * Create a discovery locator with given URI and domainId. This method is called based on
     * the value given in configuration parameter 'jrtps.discovery.announce-uris'
     * @param uri Uri 
     * @param domainId Domain ID
     * @return Discovery locator
     */
    public abstract Locator createDiscoveryLocator(URI uri, int domainId);

    /**
     * Closes TransportProvider. This method is called when participant is closed.
     * TransportProvider implementation might do some cleanup of resources by overriding 
     * this method.
     */
    public void close() { // Default implementation does nothing
    }
}
