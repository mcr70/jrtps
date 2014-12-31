package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

class CryptoToken extends DataHolder {
	CryptoToken(byte[] encryptedKeyMaterial, byte[] hmac) {
		super.binary_value1 = encryptedKeyMaterial;
		super.binary_value2 = hmac;
	}
	
	byte[] getEncryptedKeyMaterial() {
		return binary_value1;
	}
	
	byte[] getHMac() {
		return binary_value2;
	}

	@Override
	void writeTo(RTPSByteBuffer bb) {
		bb.write_long(binary_value1.length);
		bb.write(binary_value1);

		bb.write_long(binary_value2.length);
		bb.write(binary_value2);		
	}
}
