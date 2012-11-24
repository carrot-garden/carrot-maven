/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.carrotgarden.maven.aws.CarrotMojo;
import com.carrotgarden.maven.aws.util.AWSCredentialsImpl;
import com.carrotgarden.maven.aws.util.Util;

/**
 * base for cloud formation goals
 */
public abstract class CloudForm extends CarrotMojo {

	/**
	 * amazon template entry:
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
	 * > parameters-section-structure </a>
	 * 
	 */
	public static final String TEMPLATE_PARAMETERS = "Parameters";

	/**
	 * AWS CloudFormation stack name; must be unique under your aws account /
	 * region; alternatively, see {@link #stackNameProperty}
	 * 
	 * @required
	 * @parameter default-value="amazon-builder"
	 */
	private String stackName;

	/**
	 * name of project.property which, if set dynamically, will be used instead
	 * of plug-in property {@link #stackName}
	 * 
	 * @parameter
	 */
	private String stackNameProperty;

	/** prefer project.property over plug-in property */
	protected String stackName() {
		return projectValue(stackName, stackNameProperty);
	}

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
	 * when omitted, will be constructed from {@link #stackEndpintFormat} and
	 * {@link #amazonRegion}
	 * 
	 * @parameter
	 */
	private String stackEndpoint;

	/**
	 * AWS CloudFormation end point format
	 * 
	 * @parameter default-value="https://cloudformation.%s.amazonaws.com"
	 */
	private String stackEndpintFormat;

	protected String stackEndpoint() {
		return amazonEndpoint(stackEndpoint, stackEndpintFormat);
	}

	//

	protected Map<String, String> loadPluginProperties() throws Exception {

		final Map<String, String> pluginParams = new TreeMap<String, String>();

		return pluginParams;

	}

	protected Map<String, String> mergePluginProps(final Properties inputProps,
			final Map<String, String> inputParams) throws Exception {

		/** merge template parameters */
		final Map<String, String> pluginProps = new TreeMap<String, String>();

		/** from properties file */
		pluginProps.putAll(Util.safeMap(inputProps));

		/** from maven pom.xml */
		pluginProps.putAll(Util.safeMap(inputParams));

		return pluginProps;

	}

	protected CarrotCloudForm newCloudFormation( //
			final File templateFile, //
			final Map<String, String> stackParams //
	) throws Exception {

		/** */

		final String stackTemplate = safeTemplate(templateFile);

		/** */

		final Server server = settings().getServer(stackServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"settings.xml : server definition is missing for serverId="
							+ stackServerId);
		}

		final AWSCredentials credentials = new AWSCredentialsImpl(server);

		/** */

		final Logger logger = getLogger(CarrotCloudForm.class);

		/** */

		final long stackTimeout = Util.safeNumber(getLog(), this.stackTimeout,
				600);

		final CarrotCloudForm formation = new CarrotCloudForm(logger,
				stackName(), stackTemplate, stackParams, stackTimeout,
				credentials, stackEndpoint());

		return formation;

	}

	protected String safeTemplate(final File templateFile) throws Exception {
		if (templateFile == null || !templateFile.exists()) {
			return "{}";
		} else {
			return FileUtils.readFileToString(templateFile);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Set<String> loadParameterNames(final File templateFile)
			throws Exception {

		final Set<String> nameSet = new TreeSet<String>();

		if (templateFile == null || !templateFile.exists()) {
			return nameSet;
		}

		final Map templateMap = Util.jsonLoad(templateFile, Map.class);

		final Map paramMap = (Map) templateMap.get(TEMPLATE_PARAMETERS);

		if (paramMap == null) {
			return nameSet;
		}

		nameSet.addAll(paramMap.keySet());

		return nameSet;

	}

	protected Map<String, String> loadTemplateParameters(
			final File templateFile, final Map<String, String> pluginParams)
			throws Exception {

		final Map<String, String> stackParams = new TreeMap<String, String>();

		final Set<String> nameSet = loadParameterNames(templateFile);

		final Properties propsProject = project().getProperties();
		final Properties propsCommand = session().getUserProperties();
		final Properties propsSystem = session().getSystemProperties();

		for (final String name : nameSet) {

			if (pluginParams.containsKey(name)) {
				stackParams.put(name, pluginParams.get(name));
				continue;
			}

			if (propsProject.containsKey(name)) {
				stackParams.put(name, propsProject.get(name).toString());
				continue;
			}

			if (propsCommand.containsKey(name)) {
				stackParams.put(name, propsCommand.get(name).toString());
				continue;
			}

			if (propsSystem.containsKey(name)) {
				stackParams.put(name, propsSystem.get(name).toString());
				continue;
			}

		}

		return stackParams;

	}

}
