/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Mar 13, 2007
 * Time: 2:11:17 PM
 */
package org.veryquick.embweb;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ultra lightweight web server for embedding in applications
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$ Last Modified: $Date:Mar 13, 2007$ $Id: blah$
 */
public class EmbeddedServer {

	/**
	 * Logger
	 */
	public static final Logger logger = Logger.getLogger(EmbeddedServer.class
			.getName());
	/**
	 * Port to serve clients on
	 */
	private int serverPort;
	/**
	 * Server lives as long as this is true
	 */
	private volatile boolean alive;
	/**
	 * Handler
	 */
	private HttpRequestHandler clientHandler;

	/**
	 * New instance
	 *
	 * @param serverPort
	 * @throws Exception
	 */
	public static EmbeddedServer createInstance(int serverPort,
			HttpRequestHandler handler) throws Exception {
		final EmbeddedServer server = new EmbeddedServer(serverPort, handler);
		Thread thread = new Thread(new Runnable() {

			public void run() {
				try {
					server.start();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed to start server", e);
				}
			}
		}, "server thread");
		thread.start();
		return server;
	}

	/**
	 * New instance
	 *
	 * @param serverPort
	 * @param handler
	 * @throws Exception
	 */
	private EmbeddedServer(int serverPort, HttpRequestHandler handler)
			throws Exception {
		this.serverPort = serverPort;
		this.clientHandler = handler;
	}

	/**
	 * Start the server
	 *
	 * @throws java.io.IOException
	 */
	public void start() throws IOException {
		this.alive = true;
		ServerSocket serverSocket;
		if (System.getProperty("javax.net.ssl.keyStore") != null) {
			ServerSocketFactory ssocketFactory = SSLServerSocketFactory
					.getDefault();
			serverSocket = ssocketFactory.createServerSocket(this.serverPort);
		} else {
			serverSocket = new ServerSocket(this.serverPort);
		}
		logger.info("Server up on " + this.serverPort);
		while (alive) {
			Socket clientRequestSocket = serverSocket.accept();
			Thread thread = new Thread(new RequestHandler(clientRequestSocket),
					clientRequestSocket.getInetAddress().getCanonicalHostName());
			thread.start();
		}
	}

	/**
	 * Request handler
	 */
	private class RequestHandler implements Runnable {

		/**
		 * Socket to handle the request on
		 */
		private Socket clientRequestSocket;

		/**
		 * New handler
		 *
		 * @param clientRequestSocket
		 *            Socket to handle the request on
		 */
		public RequestHandler(Socket clientRequestSocket) {
			this.clientRequestSocket = clientRequestSocket;
		}

		/**
		 * Handle the request
		 *
		 * @see Thread#run()
		 */
		public void run() {
			try {
				InputStream requestInputStream = clientRequestSocket
						.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(requestInputStream));
				Map<String, String> parameters = new HashMap<String, String>();
				String url = null;
				String requestType = null;
				String line = null;
				int len = 0;
				while ((line = reader.readLine()) != null) {
					logger.config(line);

					if (line.startsWith("Content-Length: ")
							&& "POST".equalsIgnoreCase(requestType)) {
						// content read later when headers complete, need
						// content length to avoid errors
						len = Integer.parseInt(line.substring(16));
					} else if (line.startsWith("GET")
							|| line.startsWith("POST")
							|| line.startsWith("OPTIONS")) {
						StringTokenizer tokenizer = new StringTokenizer(line,
								" ");
						requestType = tokenizer.nextToken();
						url = tokenizer.nextToken();

						int indexOfQuestionMark = url.indexOf("?");
						if (indexOfQuestionMark >= 0) {
							// there are URL parameters
							String parametersToParse = url
									.substring(indexOfQuestionMark + 1);
							url = url.substring(0, indexOfQuestionMark);
							StringTokenizer parameterTokenizer = new StringTokenizer(
									parametersToParse, "&");
							while (parameterTokenizer.hasMoreTokens()) {
								String[] keyAndValue = parameterTokenizer
										.nextToken().split("=");
								if (keyAndValue.length > 1) {
									String key = "GET_"
											+ URLDecoder.decode(keyAndValue[0],
													"utf-8");
									String value = URLDecoder.decode(
											keyAndValue[1], "utf-8");
									parameters.put(key, value);
								}
							}
						}
					} else if (line.equals("")) { // headers complete

						// if "GET" break below

						// if "POST" read the content, then break
						if (requestType != null
								&& requestType.equalsIgnoreCase("POST")) {
							char[] c = new char[len];

							// cannot read line, will not complete
							// streamReader.Read(c, 0, len);
							reader.read(c, 0, len);

							String content = new String(c).trim();
							logger.info(content);

							String[] querystring = content.split("&");
							for (int i = 0; i < querystring.length; i++) {
								String[] kv = querystring[i].split("=");
								if (kv.length == 2) {
									String key = "POST_"
											+ URLDecoder.decode(kv[0], "utf-8");
									String value = URLDecoder.decode(kv[1],
											"utf-8");
									parameters.put(key, value);
								}
							}
						}

						HttpRequestHandler.Type type;
						if (requestType != null
								&& "POST".equalsIgnoreCase(requestType)) {
							type = HttpRequestHandler.POST;
						} else {
							type = HttpRequestHandler.GET;
						}
						try {
							Response response = EmbeddedServer.this.clientHandler
									.handleRequest(type, url, parameters);
							OutputStream outputStream = clientRequestSocket
									.getOutputStream();
							response.writeToStream(outputStream);
							outputStream.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, null, e);
						}
						break;
					}
				}
			} catch (SocketException e) {
				logger.log(Level.CONFIG, "Socket error", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O Error", e);
			}
		}
	}
}
