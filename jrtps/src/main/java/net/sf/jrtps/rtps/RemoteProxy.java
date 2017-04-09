package net.sf.jrtps.rtps;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class used to represent a remote entity. 
 * 
 * @author mcr70
 */
public class RemoteProxy {
    private static final Logger logger = LoggerFactory.getLogger(RemoteProxy.class);
    
    private DiscoveredData discoveredData;
    private final List<Locator> locators = new LinkedList<>();
    private boolean preferMulticast = false; // TODO: not used at the moment

    /**
     * Constructor for RemoteProxy.
     * 
     * @param dd DiscoveredData
     * @param locators a List of Locators
     */
    protected RemoteProxy(DiscoveredData dd, List<Locator> locators) {
        this.discoveredData = dd;

        // Add only locators we can handle
        for (Locator locator : locators) {
            TransportProvider provider = TransportProvider.getProviderForKind(locator.getKind());
            if (provider != null) {
                // TODO: Convert generic locator to UDPLocator, MemLocator etc.
                //       and remove all the unnecessary stuff Like InetAddress from
                //       Locator
                this.locators.add(locator);
            }
        }
    }

    /**
     * Updates DiscoveredData of this RemoteProxy
     * @param dd DiscoveredData
     */
    public void update(DiscoveredData dd) {
    	this.discoveredData = dd;
    }
    
    /**
     * Gets the Locator for the remote entity. Tries to return preferred locator
     * (unicast or multicast). If the preferred locator is null, return non
     * preferred locator. By default, unicast is preferred.
     * 
     * @return Locator
     */
    public Locator getLocator() {
        if (locators.size() > 0) {  // TODO: should we return the first one, or should we do some filtering
            return locators.get(0); // Get the first available locator
        }
        
        logger.warn("Could not find a suitable Locator from {}", locators);
        
        return null;
    }

    /**
     * Gets all the locators for this RemoteProxy
     * @return All the locators that can be handled by TransportProviders
     */
    public List<Locator> getLocators() {
        return locators;
    }
    
    /**
     * Gets the DiscoveredData associated with this Proxy.
     * 
     * @return DiscoveredData
     */
    public DiscoveredData getDiscoveredData() {
        return discoveredData;
    }

    /**
     * Return true, if remote entity represented by this RemoteProxy is
     * configured to be reliable.
     * 
     * @return true, if this RemoteProxy represents a reliable entity
     */
    public boolean isReliable() {
        QosReliability policy = getDiscoveredData().getQualityOfService().getReliability();

        return policy.getKind() == QosReliability.Kind.RELIABLE;
    }

    /**
     * Gets the Guid of remote entity.
     * 
     * @return Guid
     */
    public Guid getGuid() {
        return discoveredData.getBuiltinTopicKey();
    }

    /**
     * Gets the EntityId of remote entity. This is method behaves the same as calling
     * getGuid().getEntityId().
     * 
     * @return EntityId
     */
    public EntityId getEntityId() {
        return getGuid().getEntityId();
    }
    
    
    /**
     * Sets whether or not to prefer multicast. Default is not to prefer
     * multicast.
     * 
     * @param preferMulticast Whether or not proxy prefers multicast
     */
    public void preferMulticast(boolean preferMulticast) {
    	// BUG: this concept can be removed. Writer can determine if reader
    	// can receive multicast or not
        this.preferMulticast = preferMulticast;
    }

    @Override
    public String toString() {
        return getGuid().toString() + ", locators " + locators + ", prefers mc: " + preferMulticast;
    }
}
