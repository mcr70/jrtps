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

import net.sf.jrtps.Configuration;

public class KeyStoreAuthentication extends Authentication {
    private KeyStore ks;
    private Certificate ca;
    private X509Certificate principal;

    public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
        KeyStoreAuthentication ksa = new KeyStoreAuthentication(new Configuration());
    }
    
    public KeyStoreAuthentication(Configuration conf) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
        ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        String pwd = conf.getKeystorePassword();
        
        ks.load(is, pwd.toCharArray());
        
        ca = ks.getCertificate(conf.getSecurityCA());
        principal = (X509Certificate) ks.getCertificate(conf.getSecurityPrincipal());
        
        principal.verify(ca.getPublicKey());
    }
    
    
}
