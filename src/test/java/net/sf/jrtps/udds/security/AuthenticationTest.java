package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import net.sf.jrtps.Configuration;

import org.junit.Test;

public class AuthenticationTest {
	@Test
	public void testKeyStore() throws InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException, IOException {
		KeyStoreAuthentication ks = new KeyStoreAuthentication(new Configuration());
	}
}
