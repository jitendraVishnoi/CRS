package crs.paypal.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpInetSocketAddress;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class NVPSSLSocketFactory implements SchemeSocketFactory,
		SchemeLayeredSocketFactory {
	String mCallerId = null;

	public NVPSSLSocketFactory(String pCallerId) {
		this.mCallerId = pCallerId;
	}

	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return PPCrypto.getSSLContext(Keys.getKeyManagers(this.mCallerId))
				.getSocketFactory().createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort) throws IOException, UnknownHostException {
		return PPCrypto.getSSLContext(Keys.getKeyManagers(this.mCallerId))
				.getSocketFactory()
				.createSocket(host, port, clientHost, clientPort);
	}

	public Socket createSocket(String host, int port, InetAddress localAddress,
			int localPort, HttpClientParams params) throws IOException,
			UnknownHostException {
		SSLSocketFactory socketfactory = PPCrypto.getSSLContext(
				Keys.getKeyManagers(this.mCallerId)).getSocketFactory();

		return socketfactory.createSocket(host, port, localAddress, localPort);
	}

	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		SSLSocketFactory socketfactory = PPCrypto.getSSLContext(
				Keys.getKeyManagers(this.mCallerId)).getSocketFactory();

		return socketfactory.createSocket(socket, host, port, autoClose);
	}

	public Socket createSocket(HttpParams pHttpParams) throws IOException {
		SSLSocketFactory socketfactory = PPCrypto.getSSLContext(
				Keys.getKeyManagers(this.mCallerId)).getSocketFactory();

		return socketfactory.createSocket();
	}

	public Socket createLayeredSocket(Socket pSocket, String pTarget,
			int pPort, HttpParams pParams) throws IOException,
			UnknownHostException {
		SSLSocketFactory socketfactory = PPCrypto.getSSLContext(
				Keys.getKeyManagers(this.mCallerId)).getSocketFactory();

		return socketfactory.createSocket(pSocket, pTarget, pPort, true);
	}

	public Socket connectSocket(Socket pSock, InetSocketAddress pRemoteAddress,
			InetSocketAddress pLocalAddress, HttpParams pParams)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		if (pRemoteAddress == null) {
			throw new IllegalArgumentException("Remote address may not be null");
		}
		if (pParams == null) {
			throw new IllegalArgumentException(
					"HTTP parameters may not be null");
		}
		Socket sock = pSock != null ? pSock : PPCrypto
				.getSSLContext(Keys.getKeyManagers(this.mCallerId))
				.getSocketFactory().createSocket();

		if (pLocalAddress != null) {
			sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(pParams));
			sock.bind(pLocalAddress);
		}

		int connTimeout = HttpConnectionParams.getConnectionTimeout(pParams);
		int soTimeout = HttpConnectionParams.getSoTimeout(pParams);
		try {
			sock.setSoTimeout(soTimeout);
			sock.connect(pRemoteAddress, connTimeout);
		} catch (SocketTimeoutException ex) {
			sock.close();
			throw new ConnectTimeoutException("Connect to " + pRemoteAddress
					+ " timed out");
		}
		String hostname;
		if ((pRemoteAddress instanceof HttpInetSocketAddress))
			hostname = ((HttpInetSocketAddress) pRemoteAddress).getHttpHost()
					.getHostName();
		else
			hostname = pRemoteAddress.getHostName();
		SSLSocket sslsock;
		if ((sock instanceof SSLSocket)) {
			sslsock = (SSLSocket) sock;
		} else {
			int port = pRemoteAddress.getPort();
			sslsock = (SSLSocket) PPCrypto
					.getSSLContext(Keys.getKeyManagers(this.mCallerId))
					.getSocketFactory()
					.createSocket(sock, hostname, port, true);
		}

		return sslsock;
	}

	public boolean equals(Object obj) {
		return (obj != null)
				&& (obj.getClass().equals(NVPSSLSocketFactory.class));
	}

	public int hashCode() {
		return NVPSSLSocketFactory.class.hashCode();
	}

	public boolean isSecure(Socket pArg0) throws IllegalArgumentException {
		return true;
	}
}
