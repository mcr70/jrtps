package net.sf.jrtps.udds.security;

/**
 * See 9.3.2.3 DDS:Auth:PKI-RSA/DSA-DH HandshakeMessageToken
 *
 * @author mcr70
 */
public class HandshakeMessageToken  /* extends DataHolder */ {
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
    
    public HandshakeMessageToken() {
        this(ChallengeReq.DDS_AUTH_CHALLENGEREQ_DSA_DH);
    }
    
    HandshakeMessageToken(ChallengeReq cr) {
        class_id = cr.value;
        properties = new Property[2];
        properties[0] = new Property("dds.sec.identity", "");
        binary_value1 = "CHALLENGE".getBytes();
    }
}
