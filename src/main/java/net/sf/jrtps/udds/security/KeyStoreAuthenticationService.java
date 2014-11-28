package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
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
    private static Random random = new Random(System.currentTimeMillis()); 

    // Latches used to wait for remote participants
    private final Map<IdentityToken, CountDownLatch> handshakeLatches = new HashMap<>();
    
    private final KeyStore ks;

    private final Configuration conf;
    private final DataWriter<ParticipantStatelessMessage> statelessWriter;

    private final LocalIdentity identity;
	private volatile long psmSequenceNumber = 1; // ParticipantStatelessMessage sequence number
    
    public KeyStoreAuthenticationService(Participant p1, Configuration conf, Guid originalGuid) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, UnrecoverableKeyException {
		this.statelessWriter = null;
        this.conf = conf;

        ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        String pwd = conf.getKeystorePassword();

        ks.load(is, pwd.toCharArray());

        Certificate ca = ks.getCertificate(conf.getSecurityCA());
        if (ca == null) {
            throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
        }
        
        String alias = conf.getSecurityPrincipal();

        X509Certificate principal = (X509Certificate) ks.getCertificate(alias);
        if (principal == null) {
            throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
        }

        principal.verify(ca.getPublicKey());

        IdentityCredential identityCreadential = new IdentityCredential(principal, ks.getKey(alias, conf.getSecurityPrincipalPassword().toCharArray()));
        
        identity = new LocalIdentity(originalGuid, identityCreadential);
        
        logger.debug("Succesfully locally authenticated {}", conf.getSecurityPrincipal());
    }

    
    public X509Certificate getCertificate() {
    	return identity.getIdentityCreadential().getPrincipal();
    }

    public Guid getOriginalGuid() {
        return identity.getOriginalGuid();
    }


    /**
     * This method is called when a remote participant has been detected and it has 
     * DDS security capabilities by providing IdentityToken with ParticipantBuiltinTopicData.
     * 
     * See ch. 7.4.1.3 of DDS Security specification: Extension to RTPS Standard DCPSParticipants 
     * Builtin Topic, and ch. 9.3.3 DDS:Auth:PKI-RSA/DSA-DH plugin behavior
     * 
     * @param remoteIdentity IdentityToken of remote participant
     * @throws NoSuchAlgorithmException 
     * @throws CertificateEncodingException 
     */
    public void validateRemoteIdentity(IdentityToken remoteIdentity) throws CertificateEncodingException, NoSuchAlgorithmException {
        int comparison = identity.getIdentityToken().getEncodedHash().compareTo(remoteIdentity.getEncodedHash());
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
        ParticipantStatelessMessage psm = 
        		new ParticipantStatelessMessage(new MessageIdentity(statelessWriter.getGuid(), psmSequenceNumber++), 
        				null); // TODO: remote guid
        
        statelessWriter.write(psm);
        
    	// TODO Auto-generated method stub
    }


	LocalIdentity getLocalIdentity() {
		return identity;
	}
}
