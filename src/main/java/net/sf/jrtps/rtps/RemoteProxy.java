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
 * A base class used to represent a remote entity. Remote entity may have
 * advertised both an unicast locator and a multicast locator. This class allows
 * to set which one will be used.
 * 
 * @author mcr70
 * @see #preferMulticast(boolean)
 */
public class RemoteProxy {
    private static final Logger logger = LoggerFactory.getLogger(RemoteProxy.class);
    
    private final DiscoveredData discoveredData;
    private final List<Locator> locators = new LinkedList<>();
    private boolean preferMulticast = false;

    /**
     * Constructor for RemoteProxy.
     * 
     * @param dd
     * @param locators
     */
    protected RemoteProxy(DiscoveredData dd, List<Locator> locators) {
        this.discoveredData = dd;

        // Add only locators we can handle
        for (Locator locator : locators) {
            TransportProvider provider = TransportProvider.getInstance(locator);
            if (provider != null) {
                this.locators.add(locator);
            }
        }
    }

    /**
     * Gets the Locator for the remote entity. Tries to return preferred locator
     * (unicast or multicast). If the preferred locator is null, return non
     * preferred locator. By default, unicast is preferred.
     * 
     * @return Locator
     */
    public Locator getLocator() {
        // TODO: We should select Locator that we can handle
        
        if (preferMulticast) { // Search for multicast locator
            for (Locator loc : locators) {                
                if(TransportProvider.getInstance(loc) == null) { // Try to select this locator only if we can handle it
                    logger.debug("There was no TransportProvider registered for Locator {}, skipping it.", loc);
                    continue;
                }

                if (loc.isMulticastLocator()) {
                    return loc;
                }
            }
        } 

        for (Locator loc : locators) {
            if(TransportProvider.getInstance(loc) == null) { // Try to select this locator only if we can handle it
                continue;
            }

            return loc;
        }

        logger.warn("Could not find a suitable Locator from {}", locators);
        
        return null;
    }

    /**
     * Gets all the locators for this RemoteProxy
     * @return
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
     * @param preferMulticast
     */
    public void preferMulticast(boolean preferMulticast) {
        this.preferMulticast = preferMulticast;
    }

    public String toString() {
        return getGuid().toString() + ", locators " + locators + ", prefers mc: " + preferMulticast;
    }
}
