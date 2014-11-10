package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.Configuration;

public class KeyStoreAuthentication extends Authentication {
	private static Logger logger = LoggerFactory.getLogger(KeyStoreAuthentication.class);
	
	private KeyStore ks;
    private Certificate ca;
    private X509Certificate principal;
    
    public KeyStoreAuthentication(Configuration conf) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
        ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        String pwd = conf.getKeystorePassword();
        
        ks.load(is, pwd.toCharArray());
        
        ca = ks.getCertificate(conf.getSecurityCA());
        if (ca == null) {
        	throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityCA() + "'");
        }
        
        principal = (X509Certificate) ks.getCertificate(conf.getSecurityPrincipal());
        if (principal == null) {
        	throw new KeyStoreException("Failed to get a certificate for alias '" + conf.getSecurityPrincipal() + "'");
        }

        principal.verify(ca.getPublicKey());

        logger.debug("Succesfully locally authenticated {}", conf.getSecurityPrincipal());
    }
}
