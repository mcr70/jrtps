package net.sf.jrtps.udds.security;


/**
 * ParticipantSecurityAttributes.
 * See 8.4.2.5 for description
 * 
 * @author mcr70
 */
class ParticipantSecurityAttributes {
    private boolean allowUnauthenticatedParticipants = true;
    private boolean isAccessProtected = false;
    private boolean isRtpsProtected = false;

    public ParticipantSecurityAttributes(boolean allowUnauthenticatedParticipants,
    		boolean isAccessProtected, boolean isRtpsProtected) {
				this.allowUnauthenticatedParticipants = allowUnauthenticatedParticipants;
				this.isAccessProtected = isAccessProtected;
				this.isRtpsProtected = isRtpsProtected;
	}
    
    /**
     * Checks, whether or not unauthenticated participants will be matched
     * @return true or false
     */
    public boolean allowUnauthenticatedParticipants() {
        return allowUnauthenticatedParticipants;
    }
    
    /**
     * Checks, if AccessControl::validate_remote_permissions is used on 
     * remote participant before participant is matched with local one.
     * @return true or false
     */
    public boolean isAccessProtected() {
        return isAccessProtected;
    }
    
    /**
     * Checks, if RTPS messages will be protected.
     * @return true or false
     */
    public boolean isRtpsProtected() {
        return isRtpsProtected;
    }
}
