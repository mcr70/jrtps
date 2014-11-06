package net.sf.jrtps.udds.security;

import net.sf.jrtps.types.Guid;

class AuthenticationPlugin {
    public void validate_local_identity(IdentityCredential credential, Guid participant) throws SecurityException {
        // return identityHandle, adjusted_aprticipant_key
    }
    
    IdentityToken get_identity_token (IdentityHandle handle) throws SecurityException {
        return null;
    }

    void set_permissions_credential_and_token(IdentityHandle handle, PermissionCredential pc,
            PermissionToken pt) throws SecurityException {
        
    }
    
    void validate_remote_identity(/*...*/) {
    }
    
    void begin_handshake_request(/*...*/) {
    }

    void begin_handshake_reply(/*...*/) {
    }
    void process_handshake(/*...*/) {
    }
    
    void get_shared_secret(/*...*/) {
    }

//boolean
//    get_peer_permissions_credential_token(
//        inout PermissionsCredentialToken  permissions_credential_token,
//        in    HandshakeHandle             handshake_handle,
//        inout SecurityException           ex );
//
//boolean
//    set_listener(
//        in   AuthenticationListener  listener,
//        inout SecurityException   ex );
//
//boolean
//    return_identity_token(
//        in    IdentityToken      token,
//        inout SecurityException  ex);
//
//boolean
//    return_peer_permissions_credential_token(
//        in   PermissionsCredentialToken permissions_redential_token,
//        inout SecurityException  ex);
//
//boolean
//    return_handshake_handle(
//        in    HandshakeHandle    handshake_handle,
//        inout SecurityException  ex);
//
//boolean
//    return_identity_handle(
//        in   IdentityHandle      identity_handle,
//        inout SecurityException  ex);
//
//boolean
//    return_sharedsecret_handle(
//        in    SharedSecretHandle  sharedsecret_handle,
//        inout SecurityException   ex);


}
