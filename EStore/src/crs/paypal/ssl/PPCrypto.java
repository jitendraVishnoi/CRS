package crs.paypal.ssl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import crs.paypal.PayPalCertException;

public class PPCrypto {

	public static KeyStore p12ToKeyStore(File pClientCertFile, String pPassword)
		    throws PayPalCertException
		  {
		    KeyStore ks = null;
		    try
		    {
		      ks = KeyStore.getInstance("PKCS12", "SunJSSE");
		    } catch (KeyStoreException e) {
		      throw new PayPalCertException("KeyStoreException", e);
		    } catch (NoSuchProviderException e) {
		      throw new PayPalCertException("NoSuchProviderException", e);
		    }

		    try
		    {
		      FileInputStream in = new FileInputStream(pClientCertFile);
		      ks.load(in, pPassword.toCharArray());
		    } catch (FileNotFoundException e) {
		      throw new PayPalCertException("KeyStore FileNotFound", e);
		    } catch (NoSuchAlgorithmException e) {
		      throw new PayPalCertException("NoSuchAlgorithm", e);
		    } catch (CertificateException e) {
		      throw new PayPalCertException("CertificateException", e);
		    } catch (IOException e) {
		      throw new PayPalCertException("IOException", e);
		    }
		    return ks;
		  }

		  public static String[] readPayPalCertFile(String filename)
		    throws IOException
		  {
		    StringBuffer cert = new StringBuffer();
		    StringBuffer key = new StringBuffer();
		    StringBuffer toWrite = key;

		    BufferedReader read = new BufferedReader(new FileReader(filename));
		    String line = read.readLine();
		    while (line != null)
		    {
		      if (line.equals("")) {
		        line = read.readLine();
		      }
		      else
		      {
		        if (line.startsWith("-----BEGIN CERTIFICATE")) {
		          toWrite = cert;
		        }

		        if ((!"-----BEGIN RSA PRIVATE KEY-----".equals(line)) && (!"-----END RSA PRIVATE KEY-----".equals(line))) {
		          toWrite.append(line + "\n");
		        }

		        line = read.readLine();
		      }
		    }
		    read.close();
		    return new String[] { cert.toString(), key.toString() };
		  }

		  public static X509Certificate getUserCertFromString(String pCertPEM)
		    throws PayPalCertException
		  {
		    ByteArrayInputStream inputStream = new ByteArrayInputStream(pCertPEM.getBytes());
		    X509Certificate clientCert;
		    try
		    {
		      CertificateFactory cf = CertificateFactory.getInstance("X.509");
		      clientCert = (X509Certificate)cf.generateCertificate(inputStream);
		    } catch (CertificateException e) {
		      System.out.println("PPCrypto.getUserCertFromString: CertificateException:" + e.getMessage());
		      throw new PayPalCertException("Could not create an X509Certificate from input PEM string", e);
		    }
		    return clientCert;
		  }

		  public static SSLContext getSSLContext(KeyManager[] keymanagers)
		    throws IOException
		  {
		    try
		    {
		      SSLContext ctx = SSLContext.getInstance("SSL");
		      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		      random.setSeed(System.currentTimeMillis());

		      TrustManager[] tm = { new RelaxedX509TrustManager() };
		      ctx.init(keymanagers, tm, random);

		      return ctx;
		    } catch (Exception e) {
		      System.out.println("PPCrypto: getSSLContext exception:" + e.getMessage());
		      e.printStackTrace();
		      throw new IOException(e.getMessage());
		    }
		  }
}
