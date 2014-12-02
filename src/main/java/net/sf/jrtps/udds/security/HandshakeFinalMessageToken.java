package net.sf.jrtps.udds.security;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateEncodingException;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See 9.3.2.3.3 HandshakeFinalMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeFinalMessageToken extends DataHolder {
	private static final Logger logger = LoggerFactory.getLogger(HandshakeFinalMessageToken.class);
	private static volatile long seqNum = 0;
	
    static final String DDS_AUTH_CHALLENGEFIN_DSA_DH = "DDS:Auth:ChallengeFin:DSA‐DH";
    static final String DDS_AUTH_CHALLENGEFIN_PKI_RSA = "DDS:Auth:ChallengeFin:PKI‐RSA";
    
    public HandshakeFinalMessageToken(Guid myGuid, Guid destGuid,
    		IdentityCredential iCred, PermissionsCredential pCred) throws CertificateEncodingException {
        this(myGuid, destGuid, DDS_AUTH_CHALLENGEFIN_DSA_DH, iCred, pCred);
    }
    
    HandshakeFinalMessageToken(Guid myGuid, Guid destGuid,
    		String classId, IdentityCredential iCred, 
    		PermissionsCredential pCred) throws CertificateEncodingException {

    	super.class_id = classId;
        super.string_properties = new Property[2];
        super.string_properties[0] = new Property("dds.sec.identity", iCred.getPEMEncodedCertificate());
        
        // TODO: dds.sec.permissions is not implemented
        super.string_properties[1] = new Property("dds.sec.permissions", new String(pCred.getBinaryValue1()));
        try {
			super.binary_value1 = "CHALLENGE:".getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			logger.warn("", e); // Should not happen
		}
    }

	public HandshakeFinalMessageToken(String class_id, RTPSByteBuffer bb) {
		super.class_id = class_id;
		super.string_properties = new Property[bb.read_long()];
		for (int i = 0; i < string_properties.length; i++) {
			string_properties[i] = new Property(bb);
		}
		
		super.binary_value1 = new byte[10];
		bb.read(binary_value1);
	}
}
