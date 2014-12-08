package net.sf.jrtps.udds.security;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * See 9.3.2.3.2 HandshakeReplyMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeReplyMessageToken extends DataHolder {	
	static final String DDS_AUTH_CHALLENGEREP_DSA_DH = "DDS:Auth:ChallengeRep:DSA-DH";
    static final String DDS_AUTH_CHALLENGEREP_PKI_RSA = "DDS:Auth:ChallengeRep:PKI-RSA";
	private X509Certificate certificate;
	
    
	public HandshakeReplyMessageToken(IdentityCredential localCredentials,
			byte[] signedChallenge, byte[] challengeBytes) {
		super.class_id = DDS_AUTH_CHALLENGEREP_DSA_DH;
		super.string_properties = new Property[1];
		super.string_properties[0] = new Property("dds.sec.identity", localCredentials.getPEMEncodedCertificate());

		//String permissions = "";
		// TODO: dds.sec.permissions is not implemented
		//super.string_properties[1] = new Property("dds.sec.permissions", permissions);
		
		super.binary_value1 = challengeBytes;
		super.binary_value2 = signedChallenge;
		
		if (binary_value1 == null || binary_value2 == null) {
			throw new IllegalArgumentException("binary_value1: " + binary_value1 + " and binary_value2: " +
					binary_value2 + " cannot be null");
		}
	}


	HandshakeReplyMessageToken(String class_id, RTPSByteBuffer bb) throws CertificateException {
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
		
		super.binary_value1 = new byte[bb.read_long()];
		bb.read(binary_value1);
		
		super.binary_value2 = new byte[bb.read_long()];
		bb.read(binary_value2);
	}

	/**
	 * Gets the X509Certificate from this HandshakeRequestMessageToken.
	 * @return X509Certificate
	 */
	public X509Certificate getCertificate() {
		return certificate;
	}
	
	/**
	 * Gets the challenge offered by remote entity.
	 * @return challenge bytes
	 */
	public byte[] getChallenge() {
		return binary_value1;
	}

	/**
	 * Gets the signed challenge. Remote entity has signed local challenge.
	 * @return signed challenge
	 */
	public byte[] getSignedChallenge() {
		return binary_value2;
	}

	@Override
	void writeTo(RTPSByteBuffer bb) {
		bb.write_long(string_properties.length);
		for (Property p : string_properties) {
			p.writeTo(bb);
		}
		
		bb.write_long(binary_value1.length);
		bb.write(binary_value1);

		bb.write_long(binary_value2.length);
		bb.write(binary_value2);
	}
}
