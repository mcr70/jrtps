package net.sf.jrtps.udds.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

class LocalIdentity {
    private static MessageDigest sha256 = null;
    private final Random random = new Random(System.currentTimeMillis());
    
    private Guid guid;
    private IdentityCredential identityCreadential;
	private IdentityToken identityToken;

    LocalIdentity(IdentityCredential identityCreadential) throws NoSuchAlgorithmException {
        this.identityCreadential = identityCreadential;
        sha256 = MessageDigest.getInstance("SHA-256");
        
        String pem = identityCreadential.getPEMEncodedCertificate();
        this.identityToken = new IdentityToken(pem);
        createGuid();
    }
    

    private void createGuid() {
        byte[] prefixBytes = new byte[12];
        random.nextBytes(prefixBytes); // Initialize with random bytes
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

        byte[] hashBytes = new byte[6]; // 48 bits
        System.arraycopy(subjectNameBytes, 0, hashBytes, 0, 6); 
        BigInteger bi = new BigInteger(hashBytes);
        bi = bi.shiftRight(1); // 47 bits
        hashBytes = bi.toByteArray();
        hashBytes[0] |= 0x80;  // First bit set to 1

        System.arraycopy(hashBytes, 0, prefixBytes, 0, 6); 

        this.guid = new Guid(new GuidPrefix(prefixBytes), EntityId.PARTICIPANT);
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
    Guid getGuid() {
        return guid;
    }

    /**
     * Gets the IdentityToken of this LocalIdentity.
     * @return IdentityToken
     */
    public IdentityToken getIdentityToken() {
        return identityToken;
    }
}
