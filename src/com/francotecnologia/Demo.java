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

					/**
					 * Trim slashes
					 */
					if (url.startsWith("/")) {
						url = url.substring(1);
					}
					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}

					/**
					 * Default Home Page - /
					 */
					if (url.equals("")) {
						// Redirect
						// response.setRedirect("/datetime");
						// return response;

						// Welcome Page (Dynamically generated page)
						// url = "welcome.html";

						// File in public_html (External File)
						// File in src/embedded_public_html (Embedded File)
						url = "index.html";
					}

					/**
					 * Welcome Page (Dynamically generated content)
					 */
					if (url.equalsIgnoreCase("welcome.html")) {
						response.addContent("<html><body><h1>Welcome!</h1></body></html>");
					}
					/**
					 * Current Date Time (Dynamically generated content)
					 */
					else if (url.equalsIgnoreCase("datetime")) {
						Date dt = new Date();
						response.addContent("<html><body>Current Date Time: "
								+ dt.toString() + "</body></html>");
					}
					/**
					 * GET or POST Page (Dynamically generated content)
					 */
					else if (url.equalsIgnoreCase("getpost")) {
						response.addContent("<html><body><h1>GET n POST Page</h1>");

						if (!parameters.isEmpty()) {
							response.addContent("<h2>Result:</h2>");
							response.addContent("<ul>");
							for (String key : parameters.keySet()) {
								response.addContent("<li>" + key + " = "
										+ parameters.get(key) + "</li>");
							}
							response.addContent("</ul>");
						}

						response.addContent("<h2>GET</h2>");
						response.addContent("<a href=\"?sort=ASC&limit=10\">Test GET params</a>.");

						response.addContent("<h2>Post</h2>");
						response.addContent("<form action=\"getpost\" method=\"POST\">");
						response.addContent("Username: <input type=\"text\" name=\"username\" /><br />");
						response.addContent("Password: <input type=\"password\" name=\"pass\" /><br />");
						response.addContent("<input type=\"submit\" value=\"Send\" /></form>");

						response.addContent("</body></html>");
					}
					/**
					 * First, look at public_html (External File)
					 * 
					 * If not found, look at src/embedded_public_html
					 */
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
