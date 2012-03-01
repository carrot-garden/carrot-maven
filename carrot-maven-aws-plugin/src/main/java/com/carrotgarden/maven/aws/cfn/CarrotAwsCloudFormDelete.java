package com.carrotgarden.maven.aws.cfn;

/**
 */

import org.apache.maven.plugin.MojoFailureException;

/**
 * @description
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

			getLog().info("");

			final CloudFormation formation = getCloudFormation();

			formation.delete();

		} catch (final Exception e) {

			throw new MojoFailureException("delete failed", e);
		}

	}

}
