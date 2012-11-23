/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

/**
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;

import com.carrotgarden.maven.aws.CarrotAws;
import com.carrotgarden.maven.aws.cfn.CloudFormation;

/**
 * 
 */
public abstract class CarrotAwsSecuShel extends CarrotAws {

	/**
	 * ssh key file
	 * 
	 * @required
	 * @parameter default-value="${user.home}/.amazon/ssh-key.pem"
	 */
	protected File sshKeyFile;

	/**
	 * ssh user name
	 * 
	 * @required
	 * @parameter default-value="ubuntu"
	 */
	protected String sshUser;

	/**
	 * ssh host name
	 * 
	 * @required
	 * @parameter default-value="builder.example.com"
	 */
	protected String sshHost;

	/**
	 * ssh run expected exit success status collection; such as
	 * 
	 * @required
	 * @parameter
	 */
	protected Set<Integer> sshStatusSuccess;

	/**
	 * AWS CloudFormation stack create execution result
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/concept-outputs.html"
	 * >Outputs Section</a>
	 * 
	 * output properties file - used to substitute ssHost
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation-output.properties"
	 */
	protected File stackPropertiesOutputFile;

	/**
	 * How many times to attempt to retry ssh connection before giving up
	 * 
	 * @required
	 * @parameter default-value="5"
	 */
	protected int sshConnectRetries;

	/**
	 * How long (in seconds) to wait if a ssh connection fails before retrying
	 * 
	 * @required
	 * @parameter default-value="10"
	 */
	protected int sshConnectTimeout;

	//

	protected boolean isStatusSuccess(final Integer status) {
		return sshStatusSuccess.contains(status);
	}

	protected void ensureStatusSuccess(final Integer status) throws Exception {

		if (isStatusSuccess(status)) {
			return;
		}

		throw new IllegalStateException("invalid ssh exit status = " + status);

	}

	protected SecureShell newSecureShell() throws Exception {

		final Logger logger = getLogger(CloudFormation.class);

		final String sshHostConverted = getSSHHost();

		final SecureShell ssh = new SecureShell(logger, sshKeyFile, sshUser,
				sshHostConverted, sshConnectRetries, sshConnectTimeout);

		return ssh;

	}

	private String getSSHHost() {
		if (stackPropertiesOutputFile.exists()) {
			getLog().info("Attempt to property substitute sshHost " + sshHost);
			final Properties props = new Properties();

			try {
				final FileReader fr = new FileReader(stackPropertiesOutputFile);
				props.load(fr);
			} catch (final IOException e) {
				// Ignore exception
				getLog().warn(
						"Failed to read properties from "
								+ stackPropertiesOutputFile);
				return sshHost;
			}

			final String sshHostConverted = props.getProperty(sshHost, sshHost);
			getLog().debug("Return sshHost as " + sshHostConverted);
			return sshHostConverted;
		}
		return sshHost;
	}

}
