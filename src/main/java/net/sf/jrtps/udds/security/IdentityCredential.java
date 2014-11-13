package net.sf.jrtps.udds.security;

import java.security.Key;
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
    private final transient X509Certificate principal;
    private final transient Key privateKey;

    private final String class_id = "DDS:Auth:X.509‐PEM";
    private byte[] binary_value1;
    private byte[] binary_value2;
    
    IdentityCredential(X509Certificate principal, Key key) throws CertificateEncodingException {
        this.principal = principal;
        this.privateKey = key;
        this.binary_value1 = principal.getEncoded();
    }
    
    String getPEMEncodedCertificate() {
    	StringBuffer sb = new StringBuffer("-----BEGIN CERTIFICATE-----\n");
    	sb.append(DatatypeConverter.printBase64Binary(binary_value1));
    	sb.append("\n-----END CERTIFICATE-----");
    	
        return sb.toString();
    }
    
    X509Certificate getPrincipal() {
        return principal;
    }

    Key getPrivateKey() {
        return privateKey;
    }

    public String toString() {
    	return "IdentityCredential: " + class_id + ", " + getPEMEncodedCertificate();
    }
}
