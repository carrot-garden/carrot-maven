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
	 * cloud formation template file
	 * 
	 * @required
	 * @parameter default-value="./src/main/resources/formation.template"
	 */
	protected File templateFile;

	/**
	 * cloud formation operation timeout; seconds
	 * 
	 * @required
	 * @parameter default-value="300"
	 */
	protected Long stackTimeout;

	/**
	 * cloud formation stack name
	 * 
	 * @required
	 * @parameter default-value="amazon-tester"
	 */
	protected String stackName;

	/**
	 * cloud formation stack input parameters
	 * 
	 * @parameter
	 */
	protected Map<String, String> stackInputParams = new HashMap<String, String>();

	/**
	 * cloud formation login credentials stored in settings.xml under server id
	 * entry
	 * 
	 * @required
	 * @parameter default-value="amazon-cloud-formation"
	 */
	protected String serverId;

	//

	protected CloudFormation getCloudFormation() throws Exception {

		final String stackTemplate = FileUtils.readFileToString(templateFile);

		final Server server = settings.getServer(serverId);

		if (server == null) {
			throw new IllegalArgumentException(
					"server definition is missing for serverId=" + serverId);
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
