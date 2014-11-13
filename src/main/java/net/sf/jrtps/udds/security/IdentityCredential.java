package net.sf.jrtps.udds.security;

import java.security.Key;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

class IdentityCredential /* extends DataHolder */ {
    private final transient X509Certificate principal;
    private final transient Key privateKey;

    private final String class_id = "DDS:Auth:X.509‐PEM";
    private byte[] binary_value1;
    private byte[] binary_value2;
    
    IdentityCredential(X509Certificate principal, Key key) {
        this.principal = principal;
        this.privateKey = key;
    }
    
    String getPEMEncodedCertificate() throws CertificateEncodingException {
        StringBuffer sb = new StringBuffer();
        byte[] bytes = principal.getEncoded();
        for (int i = 0; i < bytes.length; i++) { // convert sha256 (16 bytes) to characters (32 bytes)
            sb.append(String.format("%02X", bytes[i]));
        }

        return sb.toString();
    }
    
    X509Certificate getPrincipal() {
        return principal;
    }

    Key getPrivateKey() {
        return privateKey;
    }
}
