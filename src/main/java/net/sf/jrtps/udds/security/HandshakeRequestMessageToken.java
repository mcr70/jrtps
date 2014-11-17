package net.sf.jrtps.udds.security;

import java.security.cert.CertificateEncodingException;

/**
 * See 9.3.2.3.1 HandshakeRequestMessageToken objects
 * 
 * @author mcr70
 */
public class HandshakeRequestMessageToken {
    public enum ChallengeReq {
        DDS_AUTH_CHALLENGEREQ_DSA_DH("DDS:Auth:ChallengeReq:DSA‐DH"),
        DDS_AUTH_CHALLENGEREQ_PKI_RSA("DDS:Auth:ChallengeReq:PKI‐RSA");
        
        private String value;

        ChallengeReq(String value) {
            this.value = value;
        }
    }
    
    private String class_id;
    private Property[] properties;
    private byte[] binary_value1;
    
    public HandshakeRequestMessageToken(IdentityCredential iCred, PermissionsCredential pCred) throws CertificateEncodingException {
        this(ChallengeReq.DDS_AUTH_CHALLENGEREQ_DSA_DH, iCred, pCred);
    }
    
    HandshakeRequestMessageToken(ChallengeReq cr, IdentityCredential iCred, 
    		PermissionsCredential pCred) throws CertificateEncodingException {
        class_id = cr.value;
        properties = new Property[2];
        properties[0] = new Property("dds.sec.identity", iCred.getPEMEncodedCertificate());
        
        // TODO: dds.sec.permissions is not implemented
        properties[1] = new Property("dds.sec.permissions", new String(pCred.getBinaryValue1()));
        binary_value1 = "CHALLENGE".getBytes();
    }
}
