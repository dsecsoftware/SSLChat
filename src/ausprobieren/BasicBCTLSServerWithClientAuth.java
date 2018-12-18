package ausprobieren;

import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.bouncycastle.tls.DefaultTlsServer;
import org.bouncycastle.tls.TlsServer;
import org.bouncycastle.tls.TlsServerProtocol;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;

public class BasicBCTLSServerWithClientAuth
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("StartServer");
		Security.addProvider(new BouncyCastleJsseProvider());
		Security.addProvider(new BouncyCastleProvider());

		SSLContext sslContext = SSLContext.getInstance("TLS", "BCJSSE");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX", "BCJSSE");
		kmf.init(Utils.createServerKeyStore(), Utils.SERVER_PASSWORD);

		// F�r Client Auth auch noch eine TrustManagerFactroy
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
		tmf.init(Utils.createClientTrustStore());

		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
		SSLServerSocket serverSocket = (SSLServerSocket) fact.createServerSocket(55555);

		serverSocket.setNeedClientAuth(true);

		while (true)
		{

			SSLSocket socket = (SSLSocket) serverSocket.accept();
			System.out.println("Have new Client: " + socket.getRemoteSocketAddress());
			
			TlsCrypto crypto = new BcTlsCrypto(new SecureRandom());
			TlsServer server = new DefaultTlsServer(crypto)
			{
				
				// Override e.g. TlsServer.getRSASignerCredentials() or
				// similar here, depending on what credentials you wish to use.
			};
			TlsServerProtocol protocol = new TlsServerProtocol(socket.getInputStream(), socket.getOutputStream());
			// Performs a TLS handshake
			protocol.accept(server);
			// Read/write to protocol.getInputStream(), protocol.getOutputStream()

			protocol.close();
			
		}

	}
}
