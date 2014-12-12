package net.sf.jrtps.udds.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * See 9.4.2.1 DDS:Access:PKI-Signed-XML-Permissions PermissionsCredential
 * 
 * @author mcr70
 */
class PermissionsCredential {
    static final String CLASS_ID = "DDS:Access:PKI‐Signed‐XML‐Permissions"; 
    static final String CLASS_ID_JRTPS_XML_PERMISSIONS = "jRTPS:XML‐Permissions";
    
    private String class_id;
    private byte[] binary_value1;
    
    public PermissionsCredential(InputStream xml) throws IOException {
        this.class_id = CLASS_ID_JRTPS_XML_PERMISSIONS;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while((i = xml.read()) != -1) {
            baos.write(i);
        }
        
        binary_value1 = baos.toByteArray();
    }
    
    String getClassId() {
        return class_id;
    }

    byte[] getBinaryValue1() {
        return binary_value1;
    }
}
