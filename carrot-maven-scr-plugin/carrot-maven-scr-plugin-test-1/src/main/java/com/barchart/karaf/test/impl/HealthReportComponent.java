/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.karaf.test.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.karaf.test.api.HealthReport;

//

/**
 * Provide health report servlet.
 */
@SuppressWarnings("serial")
@Component(immediate = true, service = HealthReport.class)
public class HealthReportComponent extends HttpServlet implements HealthReport {

	/**
	 * Public address URL inside AWS instance.
	 */
	public static final String AWS_URL_PUBLIC_IPV4 = "http://169.254.169.254/latest/meta-data/public-ipv4";

	/**
	 * Read string from URL.
	 */
	public static String readURL(final String textURL) {

		final StringBuilder text = new StringBuilder(128);

		try {

			final URL url = new URL(textURL);

			final HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setConnectTimeout(2 * 1000);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "config-reader");
			connection.connect();

			final InputStream input = connection.getInputStream();
			final InputStreamReader reader = new InputStreamReader(input);
			final BufferedReader buffered = new BufferedReader(reader);

			String line;
			while ((line = buffered.readLine()) != null) {
				text.append(line);
			}

			buffered.close();

		} catch (final Exception e) {
			text.append(e.getMessage());
		}

		return text.toString();

	}

	static String entry(final String key, final String value) {
		return " \"" + key + "\"" + " : " + "\"" + value + "\" ";
	}

	private HttpService httpService;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Activate
	protected void activate() throws Exception {

		log.info("activate : {}", PATH);

		httpService.registerServlet(PATH, this, null, null);

	}

	@Reference
	protected void bind(final HttpService s) {
		httpService = s;
	}

	@Deactivate
	protected void deactivate() {

		httpService.unregister(PATH);

		log.info("deactivate : {}", PATH);

	}

	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		final String report = //
		"{ " +

		entry("public-address", readURL(AWS_URL_PUBLIC_IPV4)) + "," +

		entry("current-timestamp", new DateTime().toString()) +

		" }";

		response.setContentType("application/json");

		response.getWriter().write(report);

	}

	protected void unbind(final HttpService s) {
		httpService = null;
	}

}
