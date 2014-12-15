package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import net.sf.jrtps.Configuration;

class KeystorePluginFactory extends PluginFactory {

	@Override
	public AuthenticationPlugin createAuthenticationPlugin(Configuration conf) throws PluginException {
		try {
			return new KeystoreAuthenticationPlugin(conf);
		} catch (InvalidKeyException | UnrecoverableKeyException
				| KeyStoreException | NoSuchAlgorithmException
				| CertificateException | NoSuchProviderException
				| SignatureException | NoSuchPaddingException | IOException e) {
			
			throw new PluginException("Failed to create KeystoreAuthenticationPlugin", e);
		}
	}
}
