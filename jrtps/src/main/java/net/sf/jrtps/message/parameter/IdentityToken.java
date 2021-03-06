package net.sf.jrtps.message.parameter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * IdentityToken is used with DDS security.
 * See DDS Security specification: ch. 9.3.2.2 DDS:Auth:PKI-RSA/DSA-DH IdentityToken for
 * more details.
 *
 * @author mcr70
 */
public class IdentityToken extends Parameter /* extends DataHolder */ {
    public static final String CLASS_ID_DDS_AUTH_X509_PEM_SHA256 = "DDS:Auth:X.509‐PEM‐SHA256";
    
	private String class_id;
    private byte[] binary_value1;
    
    /**
     * Constructs IdentityToken.
     * @param pem PEM encoded certificate
     * @throws NoSuchAlgorithmException id SHA-256 algorithm is not found
     */
    public IdentityToken(String pem) throws NoSuchAlgorithmException {
        super(ParameterId.PID_IDENTITY_TOKEN);
	
        this.class_id = CLASS_ID_DDS_AUTH_X509_PEM_SHA256;
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        binary_value1 = sha256.digest(pem.getBytes());
       
        if (binary_value1.length != 32) {
            throw new IllegalArgumentException("the length of encoded SHA256 hash must be 32: " + binary_value1.length);
        }
    }

    IdentityToken() {
        super(ParameterId.PID_IDENTITY_TOKEN);

    }

    /**
     * Gets the class_id of IdentityToken 
     * @return class_id
     */
    public String getClassId() {
        return class_id;
    }
    
    /**
     * Gets the binary_value1 of IdentityToken. It contains SHA256 hash
     * of binary_value1 of IdentityCredential, which contains the characters in the PEM‐encoded X.509
     * certificate for the DomainParticipant signed by the shared Certificate Authority 
     * 
     * @return SHA256 hash of DomainParticipants PEM encoded X.509 certificate
     */
    public String getEncodedHash() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < binary_value1.length; i++) {
            sb.append(String.format("%02X", binary_value1[i]));
        }

        return sb.toString();
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        class_id = bb.read_string();
        int count = bb.read_long();
        if (count != 32) {
            throw new IllegalArgumentException("the length of encoded SHA256 hash must be 32: " + count);
        }
        
        binary_value1 = new byte[count];
        bb.read(binary_value1);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_string(class_id);
        bb.write_long(binary_value1.length);
        bb.write(binary_value1);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("IdentityToken: ");
        sb.append(class_id);
        sb.append(", ");
        sb.append(getEncodedHash());
        
        return sb.toString();
    }
}
