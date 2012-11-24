/**
 * Copyright (C) 2010-201
2 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import org.apache.maven.plugin.MojoFailureException;

/**
 * unregister existing ami image
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
public class ElastiCompImageUnReg extends ElastiComp {

	/**
	 * AWS ElasticCompute existing image id to delete; also can loaded via
	 * {@link #imageIdProperty}
	 * 
	 * @required
	 * @parameter default-value="ami-12345678"
	 */
	private String imageId;

	/**
	 * name of project.property which, if set dynamically, will be used instead
	 * of static plug-in property {@link #imageId}
	 * 
	 * @parameter
	 */
	private String imageIdProperty;

	protected String imageId() throws Exception {
		return projectValue(imageId, imageIdProperty);
	}

	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("image unreg init : " + imageId());

			final CarrotElasticCompute compute = newElasticCompute();

			compute.imageUnregister(imageId());

			getLog().info("image unreg done : " + imageId());

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
