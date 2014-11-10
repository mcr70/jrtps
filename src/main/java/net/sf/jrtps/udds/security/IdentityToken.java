package net.sf.jrtps.udds.security;

import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * IdentityToken is used with DDS security.
 * See DDS Security specification: ch. 9.3.2.2 DDS:Auth:PKI-RSA/DSA-DH IdentityToken for
 * more details.
 * @author mcr70
 * 
 */
public class IdentityToken extends Parameter {
    public static final String CLASS_ID_DDS_AUTH_X509_PEM_SHA256 = "DDS:Auth:X.509‐PEM‐SHA256";
    
    private String class_id;

    private byte[] binary_value1;

    public IdentityToken(byte[] certificateHash) {
        super(ParameterId.PID_IDENTITY_TOKEN);
        this.class_id = CLASS_ID_DDS_AUTH_X509_PEM_SHA256;
        this.binary_value1 = certificateHash;
        if (certificateHash.length != 32) {
            throw new IllegalArgumentException("the length of SHA256 hash must be 32");
        }
    }

    public IdentityToken() {
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
     * @return SHA256 hash of DomainParticipants PEM encoded X.509 certificate
     */
    public byte[] getCertificateHash() {
        return binary_value1;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        class_id = bb.read_string();
        int count = bb.read_long();
        if (count != 32) {
            throw new IllegalArgumentException("the length of SHA256 hash must be 32");
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
        for (int i = 0; i < binary_value1.length; i++) {
            sb.append((char)binary_value1[i]);
        }
        
        return sb.toString();
    }
}
