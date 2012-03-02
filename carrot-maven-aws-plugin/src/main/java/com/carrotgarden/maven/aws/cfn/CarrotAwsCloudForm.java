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
	 * AWS CloudFormation template file
	 * 
	 * {@link http
	 * ://aws.amazon.com/cloudformation/aws-cloudformation-templates/}
	 * 
	 * @required
	 * @parameter default-value="./src/main/resources/formation.template"
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
	 * AWS CloudFormation stack name
	 * 
	 * @required
	 * @parameter default-value="amazon-tester"
	 */
	protected String stackName;

	/**
	 * AWS CloudFormation stack input parameters
	 * 
	 * @parameter
	 */
	protected Map<String, String> stackInputParams = new HashMap<String, String>();

	/**
	 * AWS CloudFormation login credentials stored in settings.xml under server
	 * id entry; username="aws key id", password="aws secret key";
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
