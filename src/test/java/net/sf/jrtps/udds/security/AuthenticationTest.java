package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import net.sf.jrtps.qos.AbstractQosTest;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.junit.Test;

public class AuthenticationTest extends AbstractQosTest {
	@Test
	public void testKeyStore() throws InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException, IOException {
		KeyStoreAuthentication ks = new KeyStoreAuthentication(p1, cfg1, new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.PARTICIPANT));

		System.out.println(ks.getAdjustedGuid());
		System.out.println(ks.getIdentityToken());
	}
}
