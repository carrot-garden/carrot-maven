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
 * delete existing ami image with all ebs snapshots
 * 
 * @goal elastic-compute-image-delete
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class ElastiCompImageDelete extends ElastiComp {

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

			getLog().info("image delete init : " + imageId());

			final CarrotElasticCompute compute = newElasticCompute();

			compute.imageDelete(imageId());

			getLog().info("image delete done : " + imageId());

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
