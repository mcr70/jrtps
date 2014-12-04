package net.sf.jrtps.udds.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See 9.3.2.3.2 HandshakeReplyMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeReplyMessageToken extends DataHolder {
	private static final Logger logger = LoggerFactory.getLogger(HandshakeReplyMessageToken.class);
	private static volatile long seqNum = 0;
	
    static final String DDS_AUTH_CHALLENGEREP_DSA_DH = "DDS:Auth:ChallengeRep:DSA‐DH";
    static final String DDS_AUTH_CHALLENGEREP_PKI_RSA = "DDS:Auth:ChallengeRep:PKI‐RSA";
	
    private transient IdentityCredential iCred;
    
   
    public HandshakeReplyMessageToken(HandshakeRequestMessageToken reqToken,
    		Guid myGuid, Guid destGuid,
    		IdentityCredential iCred, PermissionsCredential pCred) throws CertificateEncodingException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        this(reqToken, myGuid, destGuid, DDS_AUTH_CHALLENGEREP_DSA_DH, iCred, pCred);
    }
	
    
    HandshakeReplyMessageToken(HandshakeRequestMessageToken reqToken,
    		Guid myGuid, Guid destGuid,
    		String classId, IdentityCredential iCred, 
    		PermissionsCredential pCred) throws CertificateEncodingException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {

    	this.iCred = iCred;
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
        
        super.binary_value2 = sign(reqToken.binary_value1);
    }


	HandshakeReplyMessageToken(String class_id, RTPSByteBuffer bb) {
		super.class_id = class_id;
		super.string_properties = new Property[bb.read_long()];
		for (int i = 0; i < string_properties.length; i++) {
			string_properties[i] = new Property(bb);
		}
		
		super.binary_value1 = new byte[bb.read_long()];
		bb.read(binary_value1);
	}


	@Override
	void writeTo(RTPSByteBuffer bb) {
		bb.write_long(string_properties.length);
		for (Property p : string_properties) {
			p.writeTo(bb);
		}
		
		bb.write_long(binary_value1.length);
		bb.write(binary_value1);
	}

	/**
     * Signs binary_value1 of remote participants challenge with local private key.
     * 
     * @param binary_value1
     * @return
     * @throws SignatureException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
	private byte[] sign(byte[] binary_value1) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		Key privateKey = iCred.getPrivateKey();

		Signature signature = Signature.getInstance("SHA1withDSA");
		signature.initSign((PrivateKey)privateKey);
		
		signature.update(binary_value1);
		byte[] signatureBytes = signature.sign();
		
		return signatureBytes;
	}

	
}
