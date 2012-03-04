/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

/**
 */

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.carrotgarden.maven.aws.CarrotAws;

/**
 * 
 */
public abstract class CarrotAwsCloudForm extends CarrotAws {

	/**
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://aws.amazon.com/cloudformation/aws-cloudformation-templates"
	 * >template</a>
	 * 
	 * file
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation.template"
	 */
	protected File stackTemplateFile;

	/**
	 * AWS CloudFormation operation timeout; seconds
	 * 
	 * @required
	 * @parameter default-value="600"
	 */
	protected Long stackTimeout;

	/**
	 * AWS CloudFormation stack name; must be unique under your aws account
	 * 
	 * @required
	 * @parameter default-value="amazon-builder"
	 */
	protected String stackName;

	/**
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
	 * >Parameters Declaration</a>
	 * 
	 * stack input parameters; optional
	 * 
	 * @parameter
	 */
	protected Map<String, String> stackInputParams = new HashMap<String, String>();

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

	//

	protected CloudFormation getCloudFormation() throws Exception {

		final String stackTemplate;
		if (stackTemplateFile.exists()) {
			stackTemplate = FileUtils.readFileToString(stackTemplateFile);
		} else {
			stackTemplate = "{}";
		}

		final Server server = settings.getServer(stackServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"server definition is missing for serverId="
							+ stackServerId);
		}

		final String username = server.getUsername();
		final String password = server.getPassword();

		final Logger logger = getLogger(CloudFormation.class);

		final CloudFormation formation = new CloudFormation(logger, stackName,
				stackTemplate, stackInputParams, stackTimeout, username,
				password);

		return formation;

	}

}
