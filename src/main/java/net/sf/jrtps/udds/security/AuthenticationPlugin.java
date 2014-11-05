package net.sf.jrtps.udds.security;

import net.sf.jrtps.types.Guid;

class AuthenticationPlugin {
    public void validate_local_identity(IdentityCredential credential, Guid participant) throws SecurityException {
        // return identityHandle, adjusted_aprticipant_key
    }
}
