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
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.builtin.ParticipantStatelessMessage;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyStoreAuthentication is an Authentication plugin as discussed in
 * DDS Security specification. Chapter 9.3.3 <i>DDS:Auth:PKI-RSA/DSA-DH plugin behavior</i>
 * describes the plugin behavior.
 * 
 * @author mcr70
 */
public class KeyStoreAuthenticationService {
    private static Logger logger = LoggerFactory.getLogger(KeyStoreAuthenticationService.class);
    private static MessageDigest sha256 = null;
    private static Random random = new Random(System.currentTimeMillis()); 

    // Latches used to wait for remote participants
    private final Map<IdentityToken, CountDownLatch> handshakeLatches = new HashMap<>();
    
    private final KeyStore ks;
    private final Certificate ca;
    private final X509Certificate principal;

    private final Guid originalGuid;
    private Guid adjustedGuid;
    private IdentityToken identityToken;
    private final Configuration conf;
    private final DataWriter<ParticipantStatelessMessage> statelessWriter;
    private IdentityCredential identityCreadential;
    
    public KeyStoreAuthenticationService(Participant p1, Configuration conf, Guid guid) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, UnrecoverableKeyException {
		this.statelessWriter = null;
        this.conf = conf;
        this.originalGuid = guid;
        sha256 = MessageDigest.getInstance("MD5");

        ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        String pwd = conf.getKeystorePassword();

        ks.load(is, pwd.toCharArray());

        ca = ks.getCertificate(conf.getSecurityCA());
        if (ca == null) {
            throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
        }
        
        String alias = conf.getSecurityPrincipal();

        principal = (X509Certificate) ks.getCertificate(alias);
        if (principal == null) {
            throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
        }

        principal.verify(ca.getPublicKey());

        identityCreadential = new IdentityCredential(principal, ks.getKey(alias, conf.getSecurityPrincipalPassword().toCharArray()));
        
        adjustedGuid = getAdjustedGuid();
        LocalIdentity identity = new LocalIdentity(originalGuid, adjustedGuid, identityCreadential);
        identityToken = getIdentityToken();
        
        logger.debug("Succesfully locally authenticated {}", conf.getSecurityPrincipal());
    }


    public Guid getOriginalGuid() {
        return originalGuid;
    }

    /**
     * Gets a Guid, that should be bound to Participant. DDS Security specification restricts
     * how Guid should be constructed. If security is enabled, the Guid of the participant must be 
     * obtained by a call to this method. 
     * 
     * @return Adjusted Guid of the Participant
     */
    public Guid getAdjustedGuid() {
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
                candidateBytes = sha256.digest(originalGuid.getBytes());
                sha256.reset();     

            }

            // Next 48 bits (6 bytes) are from hash of candidate guid
            System.arraycopy(candidateBytes, 0, guidBytes, 6, 6); 

            // Last 32 bits (4 bytes) are the same as entityId of the candidate guid (I.e. EntityId.Participant)
            System.arraycopy(EntityId.PARTICIPANT.getBytes(), 0, guidBytes, 12, 4);

            adjustedGuid = new Guid(guidBytes);
        }
        return adjustedGuid;
    }

    public IdentityToken getIdentityToken() {
        if (identityToken == null) {
            byte[] bytes = adjustedGuid.getBytes();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) { // convert sha256 (16 bytes) to characters (32 bytes)
                sb.append(String.format("%02X", bytes[i]));
            }

            byte[] token = sb.toString().getBytes(); // TODO: character encoding

            this.identityToken = new IdentityToken(token);
        }

        return identityToken;
    }


    /**
     * This method is called when a remote participant has been detected and it has 
     * DDS security capabilities by providing IdentityToken with ParticipantBuiltinTopicData.
     * 
     * See ch. 7.4.1.3 of DDS Security specification: Extension to RTPS Standard DCPSParticipants 
     * Builtin Topic, and ch. 9.3.3 DDS:Auth:PKI-RSA/DSA-DH plugin behavior
     * 
     * @param remoteIdentity IdentityToken of remote participant
     */
    public void validateRemoteIdentity(IdentityToken remoteIdentity) {
        int comparison = identityToken.getEncodedHash().compareTo(remoteIdentity.getEncodedHash());
        if (comparison < 0) { // Remote is lexicographically greater
            // VALIDATION_PENDING_HANDSHAKE_REQUEST
            beginHandshakeRequest(remoteIdentity);
        }
        else if (comparison > 0) { // Remote is lexicographically smaller
            // VALIDATION_PENDING_HANDSHAKE_MESSAGE
            // Wait for remote entity to send handshae message
            CountDownLatch latch = handshakeLatches.remove(remoteIdentity);
            try {
                boolean await = latch.await(conf.getHandshakeTimeout(), TimeUnit.MILLISECONDS);
                handshakeLatches.remove(remoteIdentity);
                if (await) {
                    // TODO: then what
                }
                else {
                    logger.warn("Failed to get handshake message from remote entity on time");
                }
            } catch (InterruptedException e) {
                handshakeLatches.remove(remoteIdentity);
                logger.warn("Interrupted. Returning from validateRemoteIdentity()");
                return;
            }
        }
        else { // Remote has the same identity as local
            // ???
        }
    }

    void beginHandshakeRequest(IdentityToken remoteIdentity) {
        //ParticipantStatelessMessage psm = new ParticipantStatelessMessage(getOriginalGuid());
        //statelessWriter.write(psm);
    	// TODO Auto-generated method stub
    }
}
