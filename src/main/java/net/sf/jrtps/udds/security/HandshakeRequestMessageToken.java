package net.sf.jrtps.udds.security;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See 9.3.2.3.1 HandshakeRequestMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeRequestMessageToken extends DataHolder {
	private static final Logger logger = LoggerFactory.getLogger(HandshakeRequestMessageToken.class);

	static final String DDS_AUTH_CHALLENGEREQ_DSA_DH = "DDS:Auth:ChallengeReq:DSA-DH";
	static final String DDS_AUTH_CHALLENGEREQ_PKI_RSA = "DDS:Auth:ChallengeReq:PKI-RSA";

	private X509Certificate certificate;

//	public HandshakeRequestMessageToken(Guid myGuid, Guid destGuid,
//			IdentityCredential iCred, PermissionsCredential pCred) {
//		this(myGuid, destGuid, DDS_AUTH_CHALLENGEREQ_DSA_DH, iCred, pCred);
//	}

	HandshakeRequestMessageToken(IdentityCredential iCred, byte[] challenge) {
		
		super.class_id = DDS_AUTH_CHALLENGEREQ_DSA_DH;
		super.string_properties = new Property[1];
		super.string_properties[0] = new Property("dds.sec.identity", iCred.getPEMEncodedCertificate());

//		String permissions = "";
//		if (pCred != null) {
//			// TODO: This is not the proper way of handling this.			
//			permissions = new String(pCred.getBinaryValue1());
//		}
		
		// TODO: dds.sec.permissions is not implemented
		//super.string_properties[1] = new Property("dds.sec.permissions", permissions);
		
		super.binary_value1 = challenge;
	}

	public HandshakeRequestMessageToken(String class_id, RTPSByteBuffer bb) throws CertificateException {
		super.class_id = class_id;
		super.string_properties = new Property[bb.read_long()];
		for (int i = 0; i < string_properties.length; i++) {
			Property p = new Property(bb);
			string_properties[i] = p; 
			if ("dds.sec.identity".equals(p.getName())) {
				byte[] binary = DatatypeConverter.parseBase64Binary(p.getValue());

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				this.certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(binary));
			}
		}

		if (certificate == null) {
			throw new CertificateException("Missing string_property with name 'dds.sec.identity'");
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
	 * Gets the X509Certificate from this HandshakeRequestMessageToken.
	 * @return X509Certificate
	 */
	public X509Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Gets the challenge registered with this HandshakeRequestMessageToken. 
	 * @return challenge
	 */
	public byte[] getChallenge() {
		return binary_value1;
	}
}
