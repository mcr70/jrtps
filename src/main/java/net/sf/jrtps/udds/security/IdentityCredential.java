package net.sf.jrtps.udds.security;

import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;
/**
 * IdentityCredential.
 * See 9.3.2.1 DDS:Auth:PKI-RSA/DSA-DH IdentityCredential
 * 
 * @see net.sf.jrtps.message.parameter.IdentityToken
 * @author mcr70
 */
class IdentityCredential /* extends DataHolder */ {
    private final transient X509Certificate certificate;
    private final transient PrivateKey privateKey;

    private final String class_id = "DDS:Auth:X.509-PEM";
    private byte[] binary_value1;
    private byte[] binary_value2;
    
    IdentityCredential(X509Certificate certificate, Key key) throws CertificateEncodingException {
        this.certificate = certificate;
        this.privateKey = (PrivateKey) key;
        this.binary_value1 = certificate.getEncoded();
        this.binary_value2 = key.getEncoded();
    }
    
    /**
     * Gets the certificate as PEM (Base64) encoded.
     * @return PEM encoded certificate
     */
    String getPEMEncodedCertificate() {
    	StringBuffer sb = new StringBuffer();
        sb.append(DatatypeConverter.printBase64Binary(binary_value1));
    	
        return sb.toString();
    }
    
    X509Certificate getCertificate() {
        return certificate;
    }

    PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String toString() {
    	return "IdentityCredential: " + class_id + ", " + getPEMEncodedCertificate();
    }
}
