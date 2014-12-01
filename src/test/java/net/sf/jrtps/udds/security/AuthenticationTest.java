package net.sf.jrtps.udds.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
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
	
	@Test
	public void testSigning() throws Exception {
		byte[] bytes = new byte[] {0x01, 0x02, 0x03,0x04};
		
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        ks.load(is, "changeit".toCharArray());

        X509Certificate cert = (X509Certificate) ks.getCertificate("jrtps01");
        Key privateKey = ks.getKey("jrtps01", "jrtps01".toCharArray());
        
		Signature signature = Signature.getInstance("SHA1withDSA");
		signature.initSign((PrivateKey)privateKey);
		
		signature.update(bytes);
		byte[] signatureBytes = signature.sign();
		
		signature.initVerify(cert.getPublicKey());
		signature.verify(signatureBytes);
	}
}
