package net.sf.jrtps.udds.security;

/**
 * ParticipantSecurityAttributes.
 *
 * @author mcr70
 */
class ParticipantSecurityAttributes {
    private boolean allowUnauthenticatedParticipants = true;
    private boolean isAccessProtected = false;
    private boolean isRtpsProtected = false;

    /**
     * Checks, whether or not unauthenticated participants will be matched
     * @return true or false
     */
    public boolean allowUnauthenticatedParticipants() {
        return allowUnauthenticatedParticipants;
    }
    
    /**
     * Checks, if AccessControl plugin is used.
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
