/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import org.apache.karaf.deployer.blueprint.BlueprintTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Simple workaround for "WAR" prefix.
 */
public class WarURLHandler extends URLStreamHandler {

	private final Logger logger = LoggerFactory.getLogger(WarURLHandler.class);

	private static String SYNTAX = "war: war-uri";

	private URL warURL;

    /**
     * Open the connection for the given URL.
     *
     * @param url the url from which to open a connection.
     * @return a connection on the specified URL.
     * @throws java.io.IOException if an error occurs or if the URL is malformed.
     */
    @Override
	public URLConnection openConnection(URL url) throws IOException {
		if (url.getPath() == null || url.getPath().trim().length() == 0) {
			throw new MalformedURLException ("Path can not be null or empty. Syntax: " + SYNTAX );
		}

        // We don't resolve any specific data here, just forward it to another URL Stream Handler
		warURL = new URL(url.getPath());
		return warURL.openConnection();
	}

	public URL getWarURL() {
		return warURL;
	}

}
