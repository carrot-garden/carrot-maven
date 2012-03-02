package com.carrotgarden.maven.aws.cfn;

/**
 */

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;

/**
 * delete existing cloud formation stack by: stack name
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

			getLog().info("stack delete init");

			final CloudFormation formation = getCloudFormation();

			final Stack stack = formation.stackDelete();

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case DELETE_COMPLETE:
				break;
			default:
				throw new IllegalStateException("delete format failed");
			}

			getLog().info("stack delete done");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
