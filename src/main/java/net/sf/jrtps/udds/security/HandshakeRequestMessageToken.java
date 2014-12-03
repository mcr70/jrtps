package net.sf.jrtps.udds.security;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See 9.3.2.3.1 HandshakeRequestMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeRequestMessageToken extends DataHolder {
	private static final Logger logger = LoggerFactory.getLogger(HandshakeRequestMessageToken.class);
	private static volatile long seqNum = 0;

	static final String DDS_AUTH_CHALLENGEREQ_DSA_DH = "DDS:Auth:ChallengeReq:DSA‐DH";
	static final String DDS_AUTH_CHALLENGEREQ_PKI_RSA = "DDS:Auth:ChallengeReq:PKI‐RSA";

	public HandshakeRequestMessageToken(Guid myGuid, Guid destGuid,
			IdentityCredential iCred, PermissionsCredential pCred) {
		this(myGuid, destGuid, DDS_AUTH_CHALLENGEREQ_DSA_DH, iCred, pCred);
	}

	HandshakeRequestMessageToken(Guid myGuid, Guid destGuid,
			String classId, IdentityCredential iCred, 
			PermissionsCredential pCred) {
		
		super.class_id = classId;
		super.string_properties = new Property[2];
		super.string_properties[0] = new Property("dds.sec.identity", iCred.getPEMEncodedCertificate());

		String permissions = "";
		if (pCred != null) {
			// TODO: This is not the proper way of handling this.			
			permissions = new String(pCred.getBinaryValue1());
		}
		
		// TODO: dds.sec.permissions is not implemented
		super.string_properties[1] = new Property("dds.sec.permissions", permissions);
		try {
			super.binary_value1 = "CHALLENGE:".getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			logger.warn("", e); // Should not happen
		}
	}

	public HandshakeRequestMessageToken(String class_id, RTPSByteBuffer bb) {
		super.class_id = class_id;
		super.string_properties = new Property[bb.read_long()];
		for (int i = 0; i < string_properties.length; i++) {
			string_properties[i] = new Property(bb);
		}

		super.binary_value1 = new byte[10];
		bb.read(binary_value1);
	}

	/**
	 * Gets the X509Certificate from this HandshakeRequestMessageToken.
	 * @return X509Certificate
	 * @throws CertificateException if certificate could not be retrieved for some reason.
	 */
	public X509Certificate getCertificate() throws CertificateException {
		for (Property p : string_properties) {
			if ("dds.sec.identity".equals(p.getName())) {
				byte[] binary = DatatypeConverter.parseBase64Binary(p.getValue());

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Certificate cert = cf.generateCertificate(new ByteArrayInputStream(binary));
				return (X509Certificate) cert;
			}
		}

		throw new CertificateException("Missing string_property with name 'dds.sec.identity'");
	}

	/**
	 * Gets the challenge registered with this HandshakeRequestMessageToken. 
	 * @return challenge
	 */
	public byte[] getChallenge() {
		return binary_value1;
	}
}
