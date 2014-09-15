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
 *        CommunicationListener. This is PublicationData, if listener is
 *        to be attached to DataReader, or SubscriptionData if attached to DataWriter 
 */
public interface CommunicationListener<ENTITY_DATA extends DiscoveredData> extends DeadlineListener {
    /**
     * This method is called when a local entity has been successfully matched with
     * remote entity
     * 
     * @param ed Discovered entity data of remote entity. 
     */
    void entityMatched(ENTITY_DATA ed);

    /**
     * This method is called when a local entity cannot be matched with remote entity due to 
     * inconsistent QualityOfService.
     *  
     * @param ed Discovered entity data of remote entity.
     */
    void inconsistentQoS(ENTITY_DATA ed);
}

interface DeadlineListener {
    /**
     * This method is called when a deadline contract between local and remote entity 
     * has been violated. 
     * 
     * @param instaneceKey A key to instance that is affected
     */
    void deadlineMissed(KeyHash instanceKey);
}