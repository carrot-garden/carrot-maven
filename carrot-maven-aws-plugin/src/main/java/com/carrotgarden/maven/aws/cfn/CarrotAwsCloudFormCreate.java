package com.carrotgarden.maven.aws.cfn;

/**
 */

import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;

/**
 * @description
 * 
 * @goal cloud-formation-create
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsCloudFormCreate extends CarrotAwsCloudForm {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("");

			final CloudFormation formation = getCloudFormation();

			final Map<String, String> output = formation.create();

			if (output == null) {
				getLog().error("create failed");
				return;
			}

			getLog().info("create output");

			for (final Map.Entry<String, String> entry : output.entrySet()) {

				final String key = entry.getKey();
				final String value = entry.getValue();

				getLog().info("\n\t" + " ");
				getLog().info("\n\t" + " key=" + key);
				getLog().info("\n\t" + " value=" + value);

			}

		} catch (final Exception e) {

			throw new MojoFailureException("create failed", e);
		}

	}

}
