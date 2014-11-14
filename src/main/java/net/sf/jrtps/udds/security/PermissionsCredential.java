package net.sf.jrtps.udds.security;

/**
 * See 9.4.2.1 DDS:Access:PKI-Signed-XML-Permissions PermissionsCredential
 * 
 * @author mcr70
 */
class PermissionsCredential {
    private final String class_id = "DDS:Access:PKI‐Signed‐XML‐Permissions"; 
    
    String getClassId() {
            return class_id;
    }
    
	String getPEMEncodedSignature() {
		// TODO Auto-generated method stub
		return null;
	}
}
