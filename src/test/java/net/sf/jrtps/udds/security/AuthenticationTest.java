package net.sf.jrtps.udds.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

import net.sf.jrtps.qos.AbstractQosTest;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.junit.Test;

public class AuthenticationTest extends AbstractQosTest {
	@Test
	public void testKeyStore() throws InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException, IOException, UnrecoverableKeyException {
		KeyStoreAuthenticationService ks = new KeyStoreAuthenticationService(p1, cfg1, new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.PARTICIPANT));

		System.out.println(ks.getLocalIdentity().getOriginalGuid());
		System.out.println(ks.getLocalIdentity().getAdjustedGuid());
		System.out.println(ks.getLocalIdentity().getIdentityToken());
		System.out.println(ks.getLocalIdentity().getIdentityCredential());
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		String pem = ks.getLocalIdentity().getIdentityCredential().getPEMEncodedCertificate();
		byte[] bytes = DatatypeConverter.parseBase64Binary(pem);
		
		X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bytes));
		System.out.println("Got certificate from PEM: " + certificate.getSubjectDN());
		
		//ks.beginHandshakeRequest(null);
	}
}
