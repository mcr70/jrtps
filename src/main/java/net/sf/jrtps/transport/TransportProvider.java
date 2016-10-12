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
    public static TransportProvider getProviderForScheme(String scheme) {
        return providersForScheme.get(scheme);
    }

    /**
     * Get a provider for given Locator kind. Remote entities advertise Locators, that can be used to
     * connect to them. This method is used to get a corresponding TransportProvider if one exists.
     * 
     * @param int kind Kind of the Locator, as in Locator.getKind()
     * @return TransportProvider, or null if there was not TransportProvider registered with given Locator.kind
     */
    public static TransportProvider getProviderForKind(int kind) {
        TransportProvider transportProvider = providersForKind.get(kind);
        if (transportProvider == null) {
            logger.warn("Could not get TransportProvider for Locator kind {}", kind);
        }
        
        return transportProvider;
    }
    
    
    /**
     * Registers a TranportProvider with given scheme and kind. Scheme is used by local participant
     * to distinguish between different transports, for example UDP and HTTP. Locator kind is used
     * to match remote participants communication capabilities with given provider, for example
     * LOCATOR_KIND_UDPv4.
     * 
     * @param scheme Scheme for the provider
     * @param provider TranportProvider to register
     * @param kinds Kinds of the Locators, that will be handled by the given provider
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
     * Gets a Receiver from this TransportProvider. If the URI has a port defined, it should be used. 
     * If not, domainId, participantId and discovery parameters should be used to create port number 
     * according to specification.
     * 
     * @param locator Locator of the receiver to create.
     * @param queue a BlockingQueue, that should be populated with byte[] received by the Receiver.
     * @return Receiver, or null if Receiver could not be created
     * @throws IOException on IOException
     * 
     * @see #getPortNumberParameters()
     */
    public abstract Receiver getReceiver(Locator locator, BlockingQueue<byte[]> queue) throws IOException;

    /**
     * Gets a Transmitter. Remote entities advertise how they can be reached by the means of Locator.
     * 
     * @param locator Locator used
     * @return Writer
     * @throws IOException on IOException
     */
    public abstract Transmitter getTransmitter(Locator locator) throws IOException;
    

    /**
     * Create a Locator with given parameters. If the port of the URI
     * is not given, domainId and participantId and isDiscovery parameters are
     * used to construct port number.
     * 
     * @param uri Uri 
     * @param domainId Domain ID
     * @param participantId Participant ID
     * @param isDiscovery true or false
     * @return Discovery locator
     */
    public abstract Locator createLocator(URI uri, int domainId, int participantId, boolean isDiscovery);

    /**
     * Closes TransportProvider. This method is called when participant is closed.
     * TransportProvider implementation might do some cleanup of resources by overriding 
     * this method.
     */
    public void close() { // Default implementation does nothing
    }
}
