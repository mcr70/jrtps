package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.parameter.KeyHash;

/**
 * CommunicationListener is used by applications that need to know how  
 * of different CommunicationStatuses, as specified n DDS specification.
 * 
 * @author mcr70
 *
 * @param <ENTITY_DATA> Type of the discovered entity data, that is tracked by this 
 *        CommunicationListener. This is PublicationData, if this listener is
 *        to be attached to DataReader, or SubscriptionData if attached to DataWriter 
 */
public interface CommunicationListener<ENTITY_DATA extends DiscoveredData> {
    /**
     * This method is called when a deadline contract between remote entity and local one has been
     * violated.<p> 
     * When attached to DataReader, a call to this method is made when remote DataWriter has
     * failed to provide samples.<p>
     * When attached to DataWriter, a call to this method is made when local DataWriter
     * fails to invoke any of the write(...) methods, even though it should have.
     * 
     * @param instaneceKey A key to instance that is affected
     */
    void deadlineMissed(KeyHash instanceKey);
    
    /**
     * This method is called when a remote entity has been successfully matched with
     * local entity
     * @param ed Discovered entity data of remote entity. 
     */
    void entityMatched(ENTITY_DATA ed);

    /**
     * This method is called when a remote entity cannot be matched with local entity due to 
     * inconsistent QualityOfService. 
     * @param ed Discovered entity data of remote entity.
     */
    void inconsistentQoS(ENTITY_DATA ed);
}

