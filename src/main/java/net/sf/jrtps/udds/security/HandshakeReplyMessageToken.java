package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * See 9.3.2.3.2 HandshakeReplyMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeReplyMessageToken extends DataHolder {	
    static final String DDS_AUTH_CHALLENGEREP_DSA_DH = "DDS:Auth:ChallengeRep:DSA‐DH";
    static final String DDS_AUTH_CHALLENGEREP_PKI_RSA = "DDS:Auth:ChallengeRep:PKI‐RSA";
	
    
	public HandshakeReplyMessageToken(IdentityCredential localCredentials,
			byte[] signedChallenge, byte[] challengeBytes) {
		super.class_id = DDS_AUTH_CHALLENGEREP_DSA_DH;
		super.string_properties = new Property[1];
		super.string_properties[0] = new Property("dds.sec.identity", localCredentials.getPEMEncodedCertificate());

		//String permissions = "";
		// TODO: dds.sec.permissions is not implemented
		//super.string_properties[1] = new Property("dds.sec.permissions", permissions);
		
		binary_value1 = challengeBytes;
		binary_value2 = signedChallenge;
	}


	HandshakeReplyMessageToken(String class_id, RTPSByteBuffer bb) {
		super.class_id = class_id;
		super.string_properties = new Property[bb.read_long()];
		for (int i = 0; i < string_properties.length; i++) {
			string_properties[i] = new Property(bb);
		}
		
		super.binary_value1 = new byte[bb.read_long()];
		bb.read(binary_value1);

		super.binary_value2 = new byte[bb.read_long()];
		bb.read(binary_value2);
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
	}
}
