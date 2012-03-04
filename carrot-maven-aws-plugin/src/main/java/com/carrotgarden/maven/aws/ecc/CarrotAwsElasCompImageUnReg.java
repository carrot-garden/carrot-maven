/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

/**
 */

import org.apache.maven.plugin.MojoFailureException;

/**
 * unregister/delete existing ami image
 * 
 * @goal elastic-compute-image-unregister
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsElasCompImageUnReg extends CarrotAwsElasComp {

	/**
	 * AWS ElasticCompute ami image name; must be unique;
	 * 
	 * @required
	 * @parameter default-value="amazon-image-name"
	 */
	protected String imageName;

	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("image unreg init");

			getLog().info("image unreg done");

			throw new UnsupportedOperationException("TODO");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
