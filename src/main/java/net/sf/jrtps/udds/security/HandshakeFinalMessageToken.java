package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * See 9.3.2.3.3 HandshakeFinalMessageToken objects
 * 
 * @author mcr70
 */
class HandshakeFinalMessageToken extends DataHolder {	
    static final String DDS_AUTH_CHALLENGEFIN_DSA_DH = "DDS:Auth:ChallengeFin:DSA-DH";
    static final String DDS_AUTH_CHALLENGEFIN_PKI_RSA = "DDS:Auth:ChallengeFin:PKI-RSA";
    
	public HandshakeFinalMessageToken(IdentityCredential identityCredential,
			byte[] encryptedSharedSecret, byte[] signedData) {
		super.class_id = DDS_AUTH_CHALLENGEFIN_DSA_DH;
		super.binary_value1 = encryptedSharedSecret;
		super.binary_value2 = signedData;
	}

	HandshakeFinalMessageToken(String class_id, RTPSByteBuffer bb) {
		super.class_id = class_id;

		super.binary_value1 = new byte[bb.read_long()];
		bb.read(binary_value1);
		
		super.binary_value2 = new byte[bb.read_long()];
		bb.read(binary_value2);
	}

	/**
	 * Gets the signed data. Data that is signed, is concatenation of challenge from 
	 * handshake reply(binary_value1) and encrypted shared secret of this final
	 * handhsake message(binary_value1) 
	 */
	public byte[] getSignedData() {
		return binary_value2;
	}
	
	/**
	 * Gets the encrypted shared secret 
	 * @return encrypted shared secret
	 */
	public byte[] getEncryptedSharedSicret() {
		return binary_value1;
	}
	
	@Override
	void writeTo(RTPSByteBuffer bb) {
		bb.write_long(binary_value1.length);
		bb.write(binary_value1);

		bb.write_long(binary_value2.length);
		bb.write(binary_value2);
	}	
}
