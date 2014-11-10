package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreAuthentication extends Authentication {
	private static Logger logger = LoggerFactory.getLogger(KeyStoreAuthentication.class);
    private static MessageDigest sha256 = null;
    private static Random random = new Random(System.currentTimeMillis()); 

	private final KeyStore ks;
    private final Certificate ca;
    private final X509Certificate principal;
    
    private Guid guid;
    
    public KeyStoreAuthentication(Configuration conf) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
        sha256 = MessageDigest.getInstance("MD5");

        ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        String pwd = conf.getKeystorePassword();
        
        ks.load(is, pwd.toCharArray());
        
        ca = ks.getCertificate(conf.getSecurityCA());
        if (ca == null) {
        	throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
        }
        
        principal = (X509Certificate) ks.getCertificate(conf.getSecurityPrincipal());
        if (principal == null) {
        	throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
        }

        principal.verify(ca.getPublicKey());

        logger.debug("Succesfully locally authenticated {}", conf.getSecurityPrincipal());
        getGuid(new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.PARTICIPANT));
    }
    
    /**
     * Gets a Guid, that should be bound to Participant. DDS Security specification restricts
     * how Guid should be constructed. If security is enabled, the Guid of the participant must be 
     * obtained by a call to this method. 
     * 
     * @return Adjusted Guid of the Participant
     */
    public Guid getGuid(Guid candidateParticipantGuid) {
        byte[] guidBytes = new byte[16];
//        - The first bit (bit 0) shall be set to 1.
//        - The 47 bits following the first bit (bits 1 to 47) shall be set to
//          the 47 first bits of the SHA-256 hash of the SubjectName
//          appearing on the identity_credential
//        - The following 48 bits (bits 48 to 96) shall be set to the first 48
//          bits of the SHA-256 hash of the candidate_participant_key
//        - The remaining 32 bits (bits 97 to 127) shall be set identical to
//          the corresponding bits in the candidate_participant_key
        
        String subjectName = principal.getSubjectX500Principal().getName();
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
            candidateBytes = sha256.digest(candidateParticipantGuid.getBytes());
            sha256.reset();     
            
        }
        
        // Next 48 bits (6 bytes) are from hash of candidate guid
        System.arraycopy(candidateBytes, 0, guidBytes, 6, 6); 

        // Last 32 bits (4 bytes) are the same as entityId of the candidate guid (I.e. EntityId.Participant)
        System.arraycopy(EntityId.PARTICIPANT.getBytes(), 0, guidBytes, 12, 4);

        guid = new Guid(guidBytes);
        getIdentityToken();
        return guid;
    }
    
    public IdentityToken getIdentityToken() {
        byte[] bytes = guid.getBytes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++)
        sb.append(String.format("%02X", bytes[i]));

        byte[] token = sb.toString().getBytes(); // TODO: character encoding
        
        return new IdentityToken(token);
    }
}
