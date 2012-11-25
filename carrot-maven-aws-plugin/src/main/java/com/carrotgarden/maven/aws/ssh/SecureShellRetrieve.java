/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;

/**
 * retrieve directory content via sftp from remote to local system
 * 
 * @goal secure-shell-retrieve
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class SecureShellRetrieve extends SecureShell {

	/**
	 * ssh sftp source directory on remote file system
	 * 
	 * @required
	 * @parameter default-value="/tmp/retrieve"
	 */
	private String sshSource;

	/**
	 * ssh sftp target directory on local file system
	 * 
	 * @required
	 * @parameter default-value="./target/retrieve"
	 */
	private String sshTarget;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("");

			final CarrotSecureShell ssh = newSecureShell();

			final String source = sshSource;
			final String target = new File(sshTarget).getAbsolutePath();

			final int status = ssh.retrieve(source, target);

			assertStatusSuccess(status);

		} catch (final Exception e) {

			throw new MojoFailureException("publish failed", e);
		}

	}

}
