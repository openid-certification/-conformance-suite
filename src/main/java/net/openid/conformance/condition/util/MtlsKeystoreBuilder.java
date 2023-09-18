package net.openid.conformance.condition.util;

import net.openid.conformance.testmodule.Environment;

import javax.net.ssl.KeyManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class MtlsKeystoreBuilder {

	public static KeyManager[] configureMtls(Environment env) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
		String clientCert = env.getString("mutual_tls_authentication", "cert");
		String clientKey = env.getString("mutual_tls_authentication", "key");

		byte[] certBytes = Base64.getDecoder().decode(clientCert);
		byte[] keyBytes = Base64.getDecoder().decode(clientKey);

		X509Certificate cert = generateCertificateFromDER(certBytes);
		RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

		return new DelegatedClientKeyManager[]{new DelegatedClientKeyManager(cert, key)};
	}

	protected static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

		KeyFactory factory = KeyFactory.getInstance("RSA");

		return (RSAPrivateKey) factory.generatePrivate(spec);
	}

	protected static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");

		return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
	}

}
