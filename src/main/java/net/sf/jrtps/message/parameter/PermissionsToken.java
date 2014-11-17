package net.sf.jrtps.message.parameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.transport.RTPSByteBuffer;
/**
 * 9.4.2.3 DDS:Access:PKI-Signed-XML-Permissions PermissionsToken
 * 
 * @author mcr70
 */
public class PermissionsToken extends Parameter {
    public static final String CLASS_ID_DDS_ACCESS_PKI_SIGNED_XML_PERMISSIONS = "DDS:Access:PKI‐Signed‐XML‐Permissions";
    public static final String CLASS_ID_JRTPS_XML_PERMISSIONS = "jRTPS:XML‐Permissions";
    
    private static MessageDigest sha256;
    
    private String class_id;
    private byte[] binary_value1;
    
    /**
     * Constructs new PermissionsToken by reading the xml file from given InputStream
     * @param xml InputStream to permissions document
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public PermissionsToken(InputStream xml) throws IOException, NoSuchAlgorithmException {
        super(ParameterId.PID_PERMISSIONS_TOKEN);
        sha256 = MessageDigest.getInstance("SHA-256");
        this.class_id = CLASS_ID_JRTPS_XML_PERMISSIONS;
        
        synchronized (sha256) { // Create SHA256 of cert PEM
            byte[] bytes = new byte[1024];
            
            int i;
            while((i = xml.read()) != -1) {
                sha256.update((byte) i);
            }
            
            binary_value1 = sha256.digest();
            sha256.reset();     
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while((i = xml.read()) != -1) {
            baos.write(i);
        }
        
        binary_value1 = baos.toByteArray();        
    }

    PermissionsToken() throws NoSuchAlgorithmException {
        super(ParameterId.PID_PERMISSIONS_TOKEN);
        sha256 = MessageDigest.getInstance("SHA-256");
    }

    /*
     * Gets the class_id 
     * @return class_id
     */
    public String getClassId() {
        return class_id;
    }	

    /**
     * Gets the binary_value1
     * @return binary_value1
     */
    public byte[] getBinaryValue1() {
        return binary_value1;
    }
    
    /**
     * Gets the binary_value1 of this token. It contains SHA256 hash
     * of binary_value1 of PermissionsCredential.
     *  
     * @return SHA256 hash of binary_value1
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
        StringBuffer sb = new StringBuffer("PermissionsToken: ");
        sb.append(class_id);
        sb.append(", ");
        sb.append(getEncodedHash());
        
        return sb.toString();
    }
}
