package net.sf.jrtps.udds.security;

import net.sf.jrtps.types.Guid;

class LocalIdentity {

    private Guid originalGuid;
    private Guid adjustedGuid;
    private IdentityCredential identityCreadential;

    LocalIdentity(Guid originalGuid, Guid adjustedGuid, IdentityCredential identityCreadential) {
        this.originalGuid = originalGuid;
        this.adjustedGuid = adjustedGuid;
        this.identityCreadential = identityCreadential;
    }
    
    Guid getOriginalGuid() {
        return originalGuid;
    }
    
    Guid getAdjustedGuid() {
        return adjustedGuid;
    }
    
    IdentityCredential getIdentityCreadential() {
        return identityCreadential;
    }
}
