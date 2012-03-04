/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.karaf.deployer.blueprint.BlueprintTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As org.apache.karaf.deployer.blueprint.BlueprintURLHandler need run with OSGi container
 * so create this class only used for features-maven-plugin
 */
public class BlueprintURLHandler extends URLStreamHandler {

	private final Logger logger = LoggerFactory.getLogger(BlueprintURLHandler.class);

	private static String SYNTAX = "blueprint: bp-xml-uri";

	private URL blueprintXmlURL;

    /**
     * Open the connection for the given URL.
     *
     * @param url the url from which to open a connection.
     * @return a connection on the specified URL.
     * @throws IOException if an error occurs or if the URL is malformed.
     */
    @Override
	public URLConnection openConnection(URL url) throws IOException {
		if (url.getPath() == null || url.getPath().trim().length() == 0) {
			throw new MalformedURLException ("Path cannot be null or empty. Syntax: " + SYNTAX );
		}
		blueprintXmlURL = new URL(url.getPath());

		logger.debug("Blueprint xml URL is: [" + blueprintXmlURL + "]");
		return new Connection(url);
	}
	
	public URL getBlueprintXmlURL() {
		return blueprintXmlURL;
	}

    public class Connection extends URLConnection {

        public Connection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                BlueprintTransformer.transform(blueprintXmlURL, os);
                os.close();
                return new ByteArrayInputStream(os.toByteArray());
            } catch (Exception e) {
                logger.error("Error opening blueprint xml url", e);
                throw (IOException) new IOException("Error opening blueprint xml url").initCause(e);
            }
        }
    }

}
