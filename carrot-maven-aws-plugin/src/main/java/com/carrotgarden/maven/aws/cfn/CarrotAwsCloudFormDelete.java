/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;

/**
 * cloud formation:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/APIReference/API_DeleteStack.html"
 * >delete stack</a></b>
 * 
 * based on:
 * 
 * <b>stack name</b>;
 * 
 * ; wait for completion or fail ({@link #stackTimeout})
 * 
 * @goal cloud-formation-delete
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsCloudFormDelete extends CarrotAwsCloudForm {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("stack delete init [" + stackName + "]");

			final CloudFormation formation = getCloudFormation(null, null, null);

			final Stack stack = formation.stackDelete();

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case DELETE_COMPLETE:
				break;
			default:
				throw new IllegalStateException("stack delete failed");
			}

			getLog().info("stack delete stack=\n" + stack);

			getLog().info("stack delete done [" + stackName + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
