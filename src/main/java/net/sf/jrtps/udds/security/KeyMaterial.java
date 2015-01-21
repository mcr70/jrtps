package net.sf.jrtps.udds.security;

class KeyMaterial {
	enum CipherKind {
		NONE(0), AES128(1), AES256(2);
		
		private int kind;

		CipherKind(int kind) {
			this.kind = kind;
		}
	}
	enum HashKind {
		NONE(0), SHA1(1), SHA256(2);
		
		private int kind;

		HashKind(int kind) {
			this.kind = kind;
		}
	}
	
	private final CipherKind cipherKind;
	private final HashKind hashKind;
	private final int masterKeyId;
	
	private final byte[] masterKey; // octet[32]
	private final byte[] initializationVector; // octet[32]
	private final byte[] hmacKeyId; // octet[32]

	public KeyMaterial(CipherKind cKind, HashKind hKind, int mKeyId,
			byte[] mKey, byte[] iv, byte[] hKeyId) {
				this.cipherKind = cKind;
				this.hashKind = hKind;
				this.masterKeyId = mKeyId;
				this.masterKey = mKey;
				this.initializationVector = iv;
				this.hmacKeyId = hKeyId;
	}
}
