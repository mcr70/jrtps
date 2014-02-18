package net.sf.jrtps.rtps;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Locator;

/**
 * A base class used to represent a remote entity. Remote entity may have
 * advertised both an unicast locator and a multicast locator. This class allows
 * to set which one will be used.
 * 
 * @author mcr70
 * @see #preferMulticast(boolean)
 */
public class RemoteProxy {
    private final DiscoveredData discoveredData;

    private final Locator ucLocator;
    private final Locator mcLocator;

    private boolean preferMulticast = false;

    /**
     * Constructor for RemoteProxy.
     * 
     * @param dd
     *            DiscoveredData
     * @param ucLocator
     *            Unicast locator
     * @param mcLocator
     *            Multicast locator
     */
    protected RemoteProxy(DiscoveredData dd, Locator ucLocator, Locator mcLocator) {
        this.discoveredData = dd;
        this.ucLocator = ucLocator;
        this.mcLocator = mcLocator;
    }

    /**
     * Gets the Locator for the remote entity. Tries to return preferred locator
     * (unicast or multicast). If the preferred locator is null, return non
     * preferred locator. By default, unicast is preferred.
     * 
     * @return Locator
     */
    public Locator getLocator() {
        if (preferMulticast && mcLocator != null) {
            return mcLocator;
        } else if (!preferMulticast && ucLocator != null) {
            return ucLocator;
        }

        return ucLocator != null ? ucLocator : mcLocator;
    }

    /**
     * Gets the unicast locator of this RemoteProxy.
     * 
     * @return unicast locator, may be null
     */
    public Locator getUnicastLocator() {
        return ucLocator;
    }

    /**
     * Gets the multicast locator of this RemoteProxy.
     * 
     * @return multicast locator, may be null
     */
    public Locator getMulticastLocator() {
        return mcLocator;
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
        QosReliability policy = 
                (QosReliability) getDiscoveredData().getQualityOfService().getPolicy(QosReliability.class);

        return policy.getKind() == QosReliability.Kind.RELIABLE;
    }

    /**
     * Gets the Guid of remote entity.
     * 
     * @return Guid
     */
    public Guid getGuid() {
        return discoveredData.getKey();
    }

    /**
     * Gets the EntityId of this Endpoint. This is method behaves the same as calling
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
        return getGuid().toString() + ", uc: " + ucLocator + ", mc: " + mcLocator + ", prefers mc: " + preferMulticast;
    }
}
