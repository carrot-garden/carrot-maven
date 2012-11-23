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
import java.util.Set;

import org.slf4j.Logger;

import com.carrotgarden.maven.aws.CarrotAws;

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
	private File sshKeyFile;

	/**
	 * ssh key file property; if present, will use dynamic project.property
	 * instead of static plug-in property {@link #sshKeyFile}
	 * 
	 * @parameter
	 */
	private String sshKeyFileProperty;

	protected File sshKeyFile() {
		if (sshKeyFileProperty == null) {
			return sshKeyFile;
		} else {
			final String path = (String) project().getProperties().get(
					sshKeyFileProperty);
			return new File(path);
		}
	}

	/**
	 * ssh user name
	 * 
	 * @required
	 * @parameter default-value="ubuntu"
	 */
	private String sshUser;

	/**
	 * ssh user name property; if present, will use dynamic project.property
	 * instead of static plug-in property {@link #sshUser}
	 * 
	 * @parameter
	 */
	private String sshUserProperty;

	protected String sshUser() {
		if (sshUserProperty == null) {
			return sshUser;
		} else {
			return (String) project().getProperties().get(sshUserProperty);
		}
	}

	/**
	 * ssh host name
	 * 
	 * @required
	 * @parameter default-value="builder.example.com"
	 */
	private String sshHost;

	/**
	 * ssh host name property; if present, will use dynamic project.property
	 * instead of static plug-in property {@link #sshHost}
	 * 
	 * @parameter
	 */
	private String sshHostProperty;

	protected String sshHost() {
		if (sshHostProperty == null) {
			return sshHost;
		} else {
			return (String) project().getProperties().get(sshHostProperty);
		}
	}

	/**
	 * ssh run expected exit success status collection; such as
	 * 
	 * @required
	 * @parameter
	 */
	private Set<Integer> sshStatusSuccess;

	/**
	 * How many times to attempt to retry ssh connection before giving up
	 * 
	 * @required
	 * @parameter default-value="5"
	 */
	private int sshConnectRetries;

	/**
	 * How long (in seconds) to wait if a ssh connection fails before retrying
	 * 
	 * @required
	 * @parameter default-value="10"
	 */
	private int sshConnectTimeout;

	//

	protected boolean isStatusSuccess(final Integer status) {
		return sshStatusSuccess.contains(status);
	}

	protected void assertStatusSuccess(final Integer status) throws Exception {

		if (isStatusSuccess(status)) {
			return;
		}

		throw new IllegalStateException("invalid ssh exit status = " + status);

	}

	protected SecureShell newSecureShell() throws Exception {

		final Logger logger = getLogger(getClass());

		final SecureShell ssh = new SecureShell( //
				logger, //
				sshKeyFile(), //
				sshUser(), //
				sshHost(), //
				sshConnectRetries, //
				sshConnectTimeout //
		);

		return ssh;

	}

}
