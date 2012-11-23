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

import org.apache.maven.plugin.MojoFailureException;

/**
 * publish directory content via sftp from local to remote system
 * 
 * @goal secure-shell-publish
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsSecuShelPublish extends CarrotAwsSecuShel {

	/**
	 * ssh sftp source directory on local file system
	 * 
	 * @required
	 * @parameter default-value="./target/publish"
	 */
	protected String sshSource;

	/**
	 * ssh sftp target directory on remote file system
	 * 
	 * @required
	 * @parameter default-value="/tmp/publish"
	 */
	protected String sshTarget;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("");

			final SecureShell ssh = newSecureShell();

			final String source = new File(sshSource).getAbsolutePath();
			final String target = sshTarget;

			final int status = ssh.publish(source, target);

			ensureStatusSuccess(status);

		} catch (final Exception e) {

			throw new MojoFailureException("command failed", e);
		}

	}

}
