import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class TriggerMockServer extends AbstractHandler {
	private boolean ok;

	public TriggerMockServer(boolean ok) {
		this.ok = ok;
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("********************************************\nRequest received:\nHeader data:");
		response.setContentType("text/html;charset=utf-8");
		Enumeration<String> e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String headerName = e.nextElement();
			System.out.println(headerName + " : " + request.getHeader(headerName));
		}

		String postParams = request.getReader().readLine();
		System.out.println("\n Request data: \n " + postParams);
		if (this.ok) {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println("<h1>Hello World</h1><br>" + postParams);
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		}

	}

	public static void main(String[] args) throws Exception {
		boolean error = args.length > 0 && args[0].equals("error");

		Server server = new Server(8989);

		if (error) {
			System.out.println("Starting in error mode. Sending ERROR 500 responses.");
		} else {
			System.out.println("Starting in normal mode. Sending OK 200 responses.");
		}

		server.setHandler(new TriggerMockServer(!error));
		// configureHttps(server);
		server.start();
		server.join();

	}

	private static void configureHttps(Server server) throws Exception {
		int port = 8943;

		URL keyStoreUrl = Thread.currentThread().getContextClassLoader().getResource("testcerts/mockServerKeyStore");
		URL trustStoreUrl = Thread.currentThread().getContextClassLoader()
				.getResource("testcerts/mockServerTrustStore");

		// Setup SSL
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(keyStoreUrl.getFile());
		sslContextFactory.setKeyStorePassword("password");
		sslContextFactory.setKeyManagerPassword("testpassword");

		sslContextFactory.setTrustStorePath(trustStoreUrl.getPath());
		sslContextFactory.setTrustStorePassword("password");

		// Setup HTTP Configuration
		HttpConfiguration httpConf = new HttpConfiguration();
		httpConf.setSecurePort(port);
		httpConf.setSecureScheme("https");
		httpConf.addCustomizer(new SecureRequestCustomizer());

		ServerConnector serverConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpConf));
		serverConnector.setPort(port);

		server.setConnectors(new Connector[] { serverConnector });

	}
}
