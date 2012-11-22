/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.carrotgarden.maven.aws.CarrotAws;
import com.carrotgarden.maven.aws.util.AWSCredentialsImpl;
import com.carrotgarden.maven.aws.util.Util;

/**
 * 
 */
public abstract class CarrotAwsCloudForm extends CarrotAws {

	/**
	 * prefix for reserved template parameter names
	 * <p>
	 * plug-in properties subject to override must use this prefix and
	 * String.class type
	 */
	public static final String PREFIX = "stack";

	/**
	 * AWS CloudFormation stack name; must be unique under your aws account /
	 * region
	 * 
	 * @required
	 * @parameter default-value="amazon-builder"
	 */
	protected String stackName;

	/**
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSSecurityCredentials/1.0/AboutAWSCredentials.html"
	 * >amazon security credentials</a>
	 * 
	 * stored in
	 * 
	 * <a href=
	 * "http://www.sonatype.com/books/mvnref-book/reference/appendix-settings-sect-details.html"
	 * >maven settings.xml</a>
	 * 
	 * under server id entry; username="Access Key ID",
	 * password="Secret Access Key";
	 * 
	 * @required
	 * @parameter default-value="com.example.aws.stack"
	 */
	protected String stackServerId;

	/**
	 * AWS CloudFormation operation timeout; seconds
	 * 
	 * @required
	 * @parameter default-value="600"
	 */
	protected String stackTimeout;

	/**
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/general/latest/gr/rande.html#cfn_region"
	 * >optional api end point url</a>
	 * 
	 * which controls amazon region selection;
	 * 
	 * when omitted, will be constructed from {@link #amazonRegion}
	 * 
	 * @parameter
	 */
	protected String stackEndpoint;

	protected String getStackEndpoint() {
		if (stackEndpoint == null) {
			return "https://cloudformation." + amazonRegion + ".amazonaws.com";
		} else {
			return stackEndpoint;
		}
	}

	//

	protected Map<String, String> getStackParams(final Properties inputProps,
			final Map<String, String> inputParams) throws Exception {

		/** merge template parameters */

		final Map<String, String> stackParams = new HashMap<String, String>();

		/** from properties file */
		stackParams.putAll(safeMap(inputProps));

		/** from maven pom.xml */
		stackParams.putAll(safeMap(inputParams));

		return stackParams;

	}

	protected CloudFormation getCloudFormation(final File templateFile,
			final Map<String, String> stackParams) throws Exception {

		/** */

		final String stackTemplate = safeTemplate(templateFile);

		/** */

		final Server server = settings.getServer(stackServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"settings.xml : server definition is missing for serverId="
							+ stackServerId);
		}

		final AWSCredentials credentials = new AWSCredentialsImpl(server);

		/** */

		final Logger logger = getLogger(CloudFormation.class);

		/** */

		final long stackTimeout = safeNumber(this.stackTimeout, 600);

		final CloudFormation formation = new CloudFormation(logger, stackName,
				stackTemplate, stackParams, stackTimeout, credentials,
				getStackEndpoint());

		return formation;

	}

	protected String safeTemplate(final File templateFile) throws Exception {
		if (templateFile == null || !templateFile.exists()) {
			return "{}";
		} else {
			return FileUtils.readFileToString(templateFile);
		}
	}

	protected long safeNumber(final String numberText, final long numberDefault) {
		try {
			return Long.parseLong(numberText);
		} catch (final Throwable e) {
			getLog().warn("using numberDefault=" + numberDefault);
			return numberDefault;
		}
	}

	protected void overrideStackParams(final Map<String, String> stackParams) {

		final Set<Map.Entry<String, String>> entrySet = //
		new HashSet<Entry<String, String>>(stackParams.entrySet());

		for (final Map.Entry<String, String> entry : entrySet) {

			final String key = entry.getKey();
			final String value = entry.getValue();

			if (!key.startsWith(PREFIX)) {
				continue;
			}

			stackParams.remove(key);

			try {

				final Field field = Util.findField(this.getClass(), key);

				field.set(this, value);

				getLog().info("override : " + key + "=" + value);

			} catch (final Exception e) {

				getLog().warn("override : invalid stack param=" + key, e);

			}

		}

	}

}
