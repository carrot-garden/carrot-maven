/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import org.apache.maven.plugin.MojoFailureException;

/**
 * cloud formation:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/APIReference/API_DeleteStack.html"
 * >update stack</a></b>
 * 
 * based on:
 * 
 * <b>stack name</b>;
 * 
 * ; wait for completion or fail ({@link #stackTimeout})
 * 
 * <p>
 * note: template parameters names starting with "stack" are reserved, see
 * {@link #PREFIX}
 * 
 * @goal cloud-formation-update
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsCloudFormUpdate extends CarrotAwsCloudForm {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("stack update init [" + stackName() + "]");

			getLog().error("TODO");

			getLog().info("stack update done [" + stackName() + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
