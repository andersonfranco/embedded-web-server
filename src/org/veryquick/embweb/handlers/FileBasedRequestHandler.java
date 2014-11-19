/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Apr 10, 2007
 * Time: 4:15:37 PM
 */
package org.veryquick.embweb.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.veryquick.embweb.HttpRequestHandler;
import org.veryquick.embweb.Response;

/**
 * Handler that servers up files from a given filebase. Uses standard mime.types
 * file from a Linux distribution for non text files. Note that it buffers each
 * request into memory before serving, so it would not be suitable for
 * high-performance, large-file serving
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$ Last Modified: $Date:Apr 10, 2007$ $Id: blah$
 */
public class FileBasedRequestHandler implements HttpRequestHandler {

	/**
	 * Logger
	 */
	public static final Logger logger = Logger
			.getLogger(FileBasedRequestHandler.class.getName());

	/**
	 * Base from which to server files
	 */
	private File base;

	/**
	 * Create a new handler that serves files from a base directory
	 *
	 * @param base
	 *            directory
	 */
	public FileBasedRequestHandler(File base) {
		if (!base.isDirectory()) {
			logger.warning("public_html not found. Using current directory instead.");
			base = new File("").getAbsoluteFile();
		}
		this.base = base;
	}

	/**
	 * Handle a request
	 *
	 * @param type
	 * @param url
	 * @param parameters
	 * @return a response
	 */
	public Response handleRequest(Type type, String url,
			Map<String, String> parameters) {
		Response response = new Response();

		File fileToRead = null;
		Boolean embeddedFile = false;

		/**
		 * External Files - public_html
		 */
		fileToRead = new File(base, url);

		if (!fileToRead.exists()) {
			/**
			 * Embedded Files - embedded_public_html
			 */
			if (getClass().getResource("/embedded_public_html/" + url) != null) {
				embeddedFile = true;
			} else {
				response.setNotFound(url);
				return response;
			}
		}

		// determine mime type
		String mimeType = null;
		int indexOfDot = fileToRead.getName().indexOf('.');
		if (indexOfDot >= 0) {
			String extension = fileToRead.getName().substring(indexOfDot);
			mimeType = MimeTypeParser.getInstance().getType(extension);
		}
		if (mimeType != null) {
			response.setContentType(mimeType);
		}

		BufferedReader inTxt = null;
		BufferedInputStream inBin = null;

		try {
			int nextByte;
			if (mimeType == null || mimeType.startsWith("text")) {
				if (embeddedFile) {
					inTxt = new BufferedReader(new InputStreamReader(
							getClass().getResourceAsStream(
									"/embedded_public_html/" + url)));
				} else {
					inTxt = new BufferedReader(new InputStreamReader(
							new FileInputStream(fileToRead), "UTF-8"));
				}
				StringWriter writer = new StringWriter();
				while ((nextByte = inTxt.read()) >= 0) {
					writer.write(nextByte);
				}
				writer.close();
				response.addContent(writer.toString());
			} else {
				if (embeddedFile) {
					inBin = new BufferedInputStream(
							getClass().getResourceAsStream(
									"/embedded_public_html/" + url));
				} else {
					inBin = new BufferedInputStream(new FileInputStream(
							fileToRead));
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((nextByte = inBin.read()) >= 0) {
					out.write(nextByte);
				}
				out.close();
				response.setBinaryContent(out.toByteArray());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error reading file", e);
			response.setError(e);
			return response;
		} finally {
			try {
				if (inTxt != null) {
					inTxt.close();
				} else if (inBin != null) {
					inBin.close();
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "failed to close stream", e);
			}
		}

		response.setOk();
		return response;
	}
}
