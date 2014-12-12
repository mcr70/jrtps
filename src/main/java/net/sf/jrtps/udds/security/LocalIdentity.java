package net.sf.jrtps.udds.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;

class LocalIdentity {
    private static MessageDigest sha256 = null;

    private Guid originalGuid;
    private Guid adjustedGuid;
    private IdentityCredential identityCreadential;
	private IdentityToken identityToken;

    LocalIdentity(Guid originalGuid, IdentityCredential identityCreadential) throws NoSuchAlgorithmException {
        this.originalGuid = originalGuid;
        this.identityCreadential = identityCreadential;
        sha256 = MessageDigest.getInstance("SHA-256");
        
        String pem = identityCreadential.getPEMEncodedCertificate();
        this.identityToken = new IdentityToken(pem);
    }
    
    Guid getOriginalGuid() {
        return originalGuid;
    }
    
    
    IdentityCredential getIdentityCredential() {
        return identityCreadential; 
    }
    
    /**
     * Gets a Guid, that should be bound to Participant. DDS Security specification restricts
     * how Guid should be constructed. If security is enabled, the Guid of the participant must be 
     * obtained by a call to this method. 
     * 
     * @return Adjusted Guid of the Participant
     */
    Guid getAdjustedGuid() {
        if (adjustedGuid == null) {
            byte[] guidBytes = new byte[16];
            //        - The first bit (bit 0) shall be set to 1.
            //        - The 47 bits following the first bit (bits 1 to 47) shall be set to
            //          the 47 first bits of the SHA-256 hash of the SubjectName
            //          appearing on the identity_credential
            //        - The following 48 bits (bits 48 to 96) shall be set to the first 48
            //          bits of the SHA-256 hash of the candidate_participant_key
            //        - The remaining 32 bits (bits 97 to 127) shall be set identical to
            //          the corresponding bits in the candidate_participant_key

            String subjectName = identityCreadential.getCertificate().getSubjectX500Principal().getName();
            byte[] subjectNameBytes;
            synchronized (sha256) {
                subjectNameBytes = sha256.digest(subjectName.getBytes());  // TODO: character encoding
                sha256.reset();                
            }

            // First 48 bits (6 bytes) are from hash of subject name
            System.arraycopy(subjectNameBytes, 0, guidBytes, 0, 6); 
            // Set first bit to 0
            guidBytes[0] &= 0x7f; 

            // TODO: we could have candidate guid replaced with random bytes.
            //       This method would then need no arguments at all. Or is the 'old' 
            //       fashion guid used somewhere

            byte[] candidateBytes; 
            synchronized (sha256) { // Create SHA256 of candidate bytes
                candidateBytes = sha256.digest(originalGuid.getBytes());
                sha256.reset();     
            }

            // Next 48 bits (6 bytes) are from hash of candidate guid
            System.arraycopy(candidateBytes, 0, guidBytes, 6, 6); 

            // Last 32 bits (4 bytes) are the same as entityId of the candidate guid (I.e. EntityId.Participant)
            System.arraycopy(EntityId.PARTICIPANT.getBytes(), 0, guidBytes, 12, 4);

            this.adjustedGuid = new Guid(guidBytes);
        }
        return adjustedGuid;
    }

    /**
     * Gets the IdentityToken of this LocalIdentity.
     * @return IdentityToken
     */
    public IdentityToken getIdentityToken() {
        return identityToken;
    }
}
