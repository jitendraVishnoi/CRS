package crs.paypal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.net.ssl.KeyManagerFactory;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import crs.paypal.ssl.Keys;
import crs.paypal.ssl.NVPSSLSocketFactory;
import crs.paypal.ssl.PPCrypto;

public class PayPalConnection extends GenericService {

	private DefaultHttpClient mHttpClient;
	private String mPayPalAPIServerEndpointURL;
	private URL mURL;
	private int mPoolSize = 5;
	private String mUserName;
	private String mPassword;
	private String mSignature;
	private File mCertFile;
	private String mCertPrivateKey;
	private boolean mUseCert;
	private String mVersion = "119.0";

	public void doStartService() throws ServiceException {
		
		if (getPayPalAPIServerEndpointURL() == null) {
			throw new ServiceException(
					"payPalAPIServerEndpointURL cannot be null.");
		}
		if (getUserName() == null) {
			throw new ServiceException("userName cannot be null.");
		}
		if (getPassword() == null) {
			throw new ServiceException("password cannot be null.");
		}
		if (isUseCert()) {
			if (getCertFile() == null) {
				throw new ServiceException(
						"If you are using certificates for auth, the cert file cannot be null.");
			}
			if (getCertPrivateKey() == null) {
				throw new ServiceException(
						"If you are using certificates for auth, the cert private key cannot be null.");
			}

		} else if (getSignature() == null) {
			throw new ServiceException(
					"If you are not using certificates for auth, the signature cannot be null.");
		}

		if (isUseCert()) {
			try {
				if (Keys.containsKey(String.valueOf(hashCode()))) {
					Keys.unregisterKeys(String.valueOf(hashCode()));
				}
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				KeyStore ks = PPCrypto.p12ToKeyStore(getCertFile(),
						getCertPrivateKey());
				kmf.init(ks, getCertPrivateKey().toCharArray());
				Keys.registerKeys(String.valueOf(hashCode()),
						kmf.getKeyManagers());
			} catch (NoSuchAlgorithmException nsae) {
				if (isLoggingError()) {
					logError("PayPalConnection.doStartService: NoSuchAlgorithmException thrown at startup."
							+ nsae.getMessage());
				}

				throw new ServiceException(
						"The cryptographic algorithm for the certificate encryption is not available.");
			} catch (KeyStoreException kse) {
				if (isLoggingError()) {
					logError(
							"PayPalConnection.doStartService: KeyStoreException thrown at startup."
									+ kse.getMessage(), kse);
				}

				throw new ServiceException(
						"Keystore is not available as defined for the certificate encryption.");
			} catch (UnrecoverableKeyException uke) {
				if (isLoggingError()) {
					logError(
							"PayPalConnection.doStartService: UnrecoverableKeyException thrown at startup."
									+ uke.getMessage(), uke);
				}

				throw new ServiceException(
						"The Key used in the certificate encryption is not recoverable.");
			} catch (PayPalCertException ppce) {
				if (isLoggingError()) {
					logError(
							"PayPalConnection.doStartService: PayPalCertException thrown at startup."
									+ ppce.getMessage(), ppce);
				}

				throw new ServiceException(
						"There was a problem with the PayPal Certificate.");
			}
		}

		NVPSSLSocketFactory sf = new NVPSSLSocketFactory(
				String.valueOf(hashCode()));
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		registry.register(new Scheme("https", 443, sf));

		HttpParams httpParameters = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParameters, 360000);

		HttpConnectionParams.setSoTimeout(httpParameters, 360000);
		HttpConnectionParams.setStaleCheckingEnabled(httpParameters, false);
		HttpConnectionParams.setLinger(httpParameters, 5);
		httpParameters.setParameter("http.protocol.handle-redirects",
				Boolean.valueOf(true));
		HttpClientParams.setRedirecting(httpParameters, false);

		PoolingClientConnectionManager poolingConnectionManager = new PoolingClientConnectionManager(
				registry);
		poolingConnectionManager.setMaxTotal(getPoolSize());
		poolingConnectionManager.setDefaultMaxPerRoute(getPoolSize());
		this.mHttpClient = new DefaultHttpClient(poolingConnectionManager,
				httpParameters);

		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(
				3, true);
		this.mHttpClient.setHttpRequestRetryHandler(retryHandler);
		try {
			this.mURL = new URL(getPayPalAPIServerEndpointURL());
		} catch (MalformedURLException e) {
			throw new ServiceException(
					"payPalAPIServerEndpointURL is malformed.", e);
		}

		if (System.getProperties().get("https.proxyHost") != null) {
			String host = (String) System.getProperties()
					.get("https.proxyHost");
			int port = Integer.parseInt((String) System.getProperties().get(
					"https.proxyPort"));
			HttpHost proxy = new HttpHost(host, port);

			String username = (String) System.getProperties().get(
					"https.proxyUser");
			String password = (String) System.getProperties().get(
					"https.proxyPassword");
			if ((username != null) && (password != null)) {
				Credentials userCredentials = new UsernamePasswordCredentials(
						username, password);
				this.mHttpClient.getCredentialsProvider().setCredentials(
						new AuthScope(host, port), userCredentials);
			}
			this.mHttpClient.getParams().setParameter(
					"http.route.default-proxy", proxy);
		}
	}

	public void doStopService() throws ServiceException {
	}

	protected HashMap<String, String> call(
			HashMap<String, String> pNameValuePairs) throws HttpException,
			IOException {
		if (isLoggingDebug()) {
			logDebug("PayPalConnection.call:pNameValuePairs: "
					+ pNameValuePairs);
		}
		String response = null;
		StringBuffer encodedStringBuffer = new StringBuffer();
		for (String name : pNameValuePairs.keySet()) {
			if ((!StringUtils.isBlank(name))
					&& (pNameValuePairs.get(name) != null)) {
				encodedStringBuffer.append(name + "=");
				encodedStringBuffer.append(URLEncoder.encode(
						((String) pNameValuePairs.get(name)).toString(),
						"UTF-8")
						+ "&");
			}
		}
		encodedStringBuffer.append("VERSION="
				+ URLEncoder.encode(getVersion(), "UTF-8") + "&");
		encodedStringBuffer.append("USER="
				+ URLEncoder.encode(getUserName(), "UTF-8") + "&");
		encodedStringBuffer.append("PWD="
				+ URLEncoder.encode(getPassword(), "UTF-8") + "&");
		if (!isUseCert()) {
			encodedStringBuffer.append("SIGNATURE="
					+ URLEncoder.encode(getSignature(), "UTF-8") + "&");
		}
		encodedStringBuffer.append("BUTTONSOURCE="
				+ URLEncoder.encode("SparkRed_ATG_EC_US", "UTF-8") + "&");

		if (isLoggingDebug()) {
			logDebug("PayPalConnection.call:encodedString: "
					+ encodedStringBuffer.toString());
		}
		vlogDebug("encodedStringBuffer :: {0}", encodedStringBuffer);
		HttpPost httpPost = new HttpPost(this.mURL.getPath());
		StringEntity messageEntity = new StringEntity(
				encodedStringBuffer.toString(), ContentType.create("text/html",
						"UTF-8"));

		httpPost.setEntity(messageEntity);
		try {
			HttpHost targetHost = new HttpHost(this.mURL.getHost(),
					this.mURL.getPort(), this.mURL.getProtocol());
			HttpResponse httpResponse = this.mHttpClient.execute(targetHost,
					httpPost);
			int result = httpResponse.getStatusLine().getStatusCode();
			if (result == 200) {
				response = EntityUtils.toString(httpResponse.getEntity());
				if (isLoggingDebug())
					logDebug("PayPalConnection.call:response: " + response);
			} else {
				if (isLoggingError()) {
					logError("PayPalConnection.call: Call failed. response status was "
							+ result);
				}
				throw new HttpException("Http response status was " + result);
			}
		} finally {
			httpPost.releaseConnection();
		}
		StringTokenizer stTok = new StringTokenizer(response, "&");
		HashMap<String, String> responseMap = new HashMap<String, String>();
		while (stTok.hasMoreTokens()) {
			StringTokenizer stInternalTokenizer = new StringTokenizer(
					stTok.nextToken(), "=");
			if (stInternalTokenizer.countTokens() == 2) {
				responseMap.put(stInternalTokenizer.nextToken(), URLDecoder
						.decode(stInternalTokenizer.nextToken(), "UTF-8"));
			}
		}

		return responseMap;
	}

	public void setPoolSize(int pPoolSize) {
		this.mPoolSize = pPoolSize;
	}

	public int getPoolSize() {
		return this.mPoolSize;
	}

	public String getUserName() {
		return this.mUserName;
	}

	public void setUserName(String pUserName) {
		this.mUserName = pUserName;
	}

	public String getPassword() {
		return this.mPassword;
	}

	public void setPassword(String pPassword) {
		this.mPassword = pPassword;
	}

	public String getSignature() {
		return this.mSignature;
	}

	public void setSignature(String pSignature) {
		this.mSignature = pSignature;
	}

	public boolean isUseCert() {
		return this.mUseCert;
	}

	public void setUseCert(boolean pUseCert) {
		this.mUseCert = pUseCert;
	}

	public String getCertPrivateKey() {
		return this.mCertPrivateKey;
	}

	public void setCertPrivateKey(String pCertPrivateKey) {
		this.mCertPrivateKey = pCertPrivateKey;
	}

	public File getCertFile() {
		return this.mCertFile;
	}

	public void setCertFile(File pCertFile) {
		this.mCertFile = pCertFile;
	}

	public void setPayPalAPIServerEndpointURL(String pPayPalAPIServerEndpointURL) {
		this.mPayPalAPIServerEndpointURL = pPayPalAPIServerEndpointURL;
	}

	public String getPayPalAPIServerEndpointURL() {
		return this.mPayPalAPIServerEndpointURL;
	}

	public String getVersion() {
		return this.mVersion;
	}

	public void setVersion(String pVersion) {
		this.mVersion = pVersion;
	}

}
