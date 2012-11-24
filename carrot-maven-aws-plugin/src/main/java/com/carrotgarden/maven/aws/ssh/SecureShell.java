/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import com.carrotgarden.maven.aws.CarrotMojo;

/**
 * base for ssh goals
 */
public abstract class SecureShell extends CarrotMojo {

	/**
	 * ssh key file
	 * 
	 * @required
	 * @parameter default-value="${user.home}/.amazon/ssh-key.pem"
	 */
	private String sshKeyFile;

	/**
	 * ssh key file property; if present, will use dynamic project.property
	 * instead of static plug-in property {@link #sshKeyFile}
	 * 
	 * @parameter
	 */
	private String sshKeyFileProperty;

	protected File sshKeyFile() {
		final String path = projectValue(sshKeyFile, sshKeyFileProperty);
		return new File(path);
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
		return projectValue(sshUser, sshUserProperty);
	}

	/**
	 * ssh host port
	 * 
	 * @parameter default-value="22"
	 */
	private String sshPort;

	/**
	 * ssh host port property; if present, will use dynamic project.property
	 * instead of static plug-in property {@link #sshPort}
	 * 
	 * @parameter
	 */
	private String sshPortProperty;

	protected Integer sshPort() {
		final String text = projectValue(sshPort, sshPortProperty);
		return Integer.parseInt(text);
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
		return projectValue(sshHost, sshHostProperty);
	}

	/**
	 * ssh run expected exit success status collection; contains 0 by default
	 * 
	 * @parameter
	 */
	private Set<Integer> sshStatusSuccess;
	{
		sshStatusSuccess = new HashSet<Integer>();
		sshStatusSuccess.add(0);
	}

	/**
	 * How many times to attempt to retry ssh connection before giving up
	 * 
	 * @parameter default-value="5"
	 */
	private int sshConnectRetries;

	/**
	 * How long (in seconds) to wait if a ssh connection fails before retrying
	 * 
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

	protected CarrotSecureShell newSecureShell() throws Exception {

		final Logger logger = getLogger(getClass());

		final CarrotSecureShell ssh = new CarrotSecureShell( //
				logger, //
				sshKeyFile(), //
				sshUser(), //
				sshHost(), //
				sshPort(), //
				sshConnectRetries, //
				sshConnectTimeout //
		);

		return ssh;

	}

}
