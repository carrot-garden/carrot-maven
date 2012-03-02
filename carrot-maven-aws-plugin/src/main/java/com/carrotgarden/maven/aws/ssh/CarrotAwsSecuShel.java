package com.carrotgarden.maven.aws.ssh;

/**
 */

import java.io.File;
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

	protected SecureShell getSecureShell() throws Exception {

		final Logger logger = getLogger(CloudFormation.class);

		final SecureShell ssh = new SecureShell(logger, sshKeyFile, sshUser,
				sshHost);

		return ssh;

	}

}
