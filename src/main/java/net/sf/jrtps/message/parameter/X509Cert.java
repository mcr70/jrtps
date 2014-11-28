package net.sf.jrtps.message.parameter;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Represents X.509 Certificate.
 * 
 * @author mcr70
 */
public class X509Cert extends Parameter implements JRTPSSpecificParameter {

    private byte[] certBytes;

    public X509Cert(X509Certificate certificate) throws CertificateEncodingException {
        super(ParameterId.PID_X509CERT);
        this.certBytes = certificate.getEncoded();
    }
    
    X509Cert() {
        super(ParameterId.PID_X509CERT);
    }
    
    /**
     * Gets the X509Certificate
     * @return X509Certificate
     * @throws CertificateException
     */
    public X509Certificate getCertificate() throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        
        return certificate;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        certBytes = new byte[bb.read_long()];
        bb.read(certBytes);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(certBytes.length);
        bb.write(certBytes);
    }
}
