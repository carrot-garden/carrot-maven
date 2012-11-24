/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

import org.apache.maven.plugin.MojoFailureException;

/**
 * execute remote ssh commands
 * 
 * @goal secure-shell-execute
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class SecureShellExecute extends SecureShell {

	/**
	 * ssh exec command
	 * 
	 * @required
	 * @parameter default-value="ls -las"
	 */
	private String sshCommand;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("");

			final CarrotSecureShell ssh = newSecureShell();

			final int status = ssh.execute(sshCommand);

			assertStatusSuccess(status);

		} catch (final Exception e) {

			throw new MojoFailureException("command failed", e);

		}

	}

}
