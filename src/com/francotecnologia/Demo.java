package com.francotecnologia;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.veryquick.embweb.EmbeddedServer;
import org.veryquick.embweb.Response;
import org.veryquick.embweb.handlers.FileBasedRequestHandler;

public class Demo {

	public static void main(String[] args) {

		try {
			EmbeddedServer.createInstance(8090, new FileBasedRequestHandler(
					new File("public_html")) {
				public Response handleRequest(Type type, String url,
						Map<String, String> parameters) {
					Response response = new Response();

					// Trim slashes
					if (url.startsWith("/")) {
						url = url.substring(1);
					}
					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}

					// Default Home Page
					if (url.equals("")) {
						// Redirect
						// response.setRedirect("/datetime");
						// return response;

						// Welcome Page (Internal Content)
						// url = "welcome.html";

						// File in public_html (External Content)
						url = "index.html";
					}

					// Welcome Page (Internal Content)
					if (url.equalsIgnoreCase("welcome.html")) {
						response.addContent("<html><body>Welcome!</body></html>");
					}
					// Current Date Time (Internal Content)
					else if (url.equalsIgnoreCase("datetime")) {
						Date dt = new Date();
						response.addContent("<html><body>Current Date Time: "
								+ dt.toString() + "</body></html>");
					}
					// Files in public_html (External Content)
					else {
						return super.handleRequest(type, url, parameters);
					}

					response.setOk();
					return response;
				}
			});
		} catch (Exception ex) {
			Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

}
