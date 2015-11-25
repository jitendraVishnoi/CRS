package crs.paypal.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

class RelaxedX509TrustManager implements X509TrustManager {
	public boolean checkClientTrusted(X509Certificate[] chain) {
		return true;
	}

	public boolean isServerTrusted(X509Certificate[] chain) {
		return true;
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) {
	}
}