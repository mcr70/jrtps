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
public class PermissionsToken extends Parameter {
    public static final String CLASS_ID_DDS_ACCESS_PKI_SIGNED_XML_PERMISSIONS_SHA256 = "DDS:Access:PKI‐Signed‐XML‐Permissions";
    
    private String class_id;

    private byte[] binary_value1;

    public PermissionsToken(byte[] certificateHash) {
        super(ParameterId.PID_IDENTITY_TOKEN);
        this.binary_value1 = certificateHash;
        if (certificateHash.length != 32) {
            throw new IllegalArgumentException("the length of SHA256 hash must be 32");
        }
    }

    public PermissionsToken() {
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
     * Gets the binary_value1 of PermissionsToken. It contains SHA256 hash
     * of binary_value1 of PermissionsCredential, which contains the characters in the PEM‐encoded PKCS#7
     * signature of the XML permissions document for DomainParticipant 
     * @return SHA256 hash of signature of permissions document
     */
    public byte[] getPermissionDocumentSignatureHash() {
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
}
