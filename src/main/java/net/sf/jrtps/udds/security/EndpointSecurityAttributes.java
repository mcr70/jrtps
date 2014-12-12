package net.sf.jrtps.udds.security;


/**
 * EndpointSecurityAttributes.
 * 
 * @author mcr70
 */
class EndpointSecurityAttributes {
    private boolean isAccessProtected = false;
    private boolean isDiscoveryProtected = false;
    private boolean isSubmessageProtected = false;
    private boolean isPayloadProtected = false;

    /**
     * Checks, if entity is protected by AccessControl
     * @return true or false
     */
    public boolean isAccessProtected() {
        return isAccessProtected;
    }
    
    /**
     * Checks if discovery data is sent using secure topics or
     * regular topics.
     * 
     * @return true or false
     */
    public boolean isDiscoveryProtected() {
        return isDiscoveryProtected;
    }
    
    /**
     * Checks whether or not entity will protect submessages.
     * @return true or false
     */
    public boolean isSubmessageProtected() {
        return isSubmessageProtected;
    }
    
    /**
     * Checks whether or not payload is secured or not.
     * @return
     */
    public boolean isPayloadProtected() {
        return isPayloadProtected;
    }
}
