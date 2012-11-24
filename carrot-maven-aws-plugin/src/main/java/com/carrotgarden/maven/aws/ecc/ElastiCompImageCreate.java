/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Image;
import com.carrotgarden.maven.aws.ecc.CarrotElasticCompute.State;

/**
 * create new ami image from existing AWS ElasticCompute instance
 * 
 * @goal elastic-compute-image-create
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class ElastiCompImageCreate extends ElastiComp {

	/**
	 * name of project.property which will contain {@link Image} instance after
	 * execution of this maven goal, which can be used by groovy script as
	 * follows:
	 * 
	 * <pre>
	 * def image = project.properties["amazonImage"]
	 * println "name  = " + image.name
	 * println "state = " + image.state
	 * </pre>
	 * 
	 * @required
	 * @parameter default-value="amazonImage"
	 */
	private String imageResultProperty;

	/**
	 * name of project.property which will contain image id after this execution
	 * 
	 * @required
	 * @parameter default-value="amazonImageId"
	 */
	private String imageIdResultProperty;

	/**
	 * AWS ElasticCompute existing instance id to create image from; also can
	 * loaded via {@link #imageInstanceIdProperty}
	 * 
	 * @required
	 * @parameter default-value="i-12345678"
	 */
	private String imageInstanceId;

	/**
	 * name of project.property which, if set dynamically, will be used instead
	 * of static plug-in property {@link #imageInstanceId}
	 * 
	 * @parameter
	 */
	private String imageInstanceIdProperty;

	protected String imageInstanceId() throws Exception {
		return projectValue(imageInstanceId, imageInstanceIdProperty);
	}

	/**
	 * AWS ElasticCompute AMI image name; must be unique under your aws account;
	 * used to tag resources; can be loaded from {@link #imageNameProperty}
	 * 
	 * @parameter default-value="amazon-image-name"
	 */
	private String imageName;

	/**
	 * name of project.property which, if set dynamically, will be used instead
	 * of plug-in property {@link #imageName}
	 * 
	 * @parameter
	 */
	private String imageNameProperty;

	protected String imageName() {
		return projectValue(imageName, imageNameProperty);
	}

	/**
	 * AWS ElasticCompute ami image description
	 * 
	 * @parameter default-value="amazon-image-description"
	 */
	private String imageDescription;

	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("image create init [" + imageName() + "]");

			final CarrotElasticCompute compute = newElasticCompute();

			final Image image = compute.imageCreate( //
					imageInstanceId(), //
					imageName(), //
					imageDescription //
					);

			final State state = State.fromValue(image.getState());

			switch (state) {
			case AVAILABLE:
				break;
			default:
				throw new IllegalStateException("image create failed : \n"
						+ image);
			}

			final String imageId = image.getImageId();

			/** publish result */
			project().getProperties().put(imageResultProperty, image);
			project().getProperties().put(imageIdResultProperty, imageId);

			/** tag image */
			compute.tagCreate(imageId, amazonTagName(), imageName());

			/** tag image devices */
			for (final BlockDeviceMapping blockDevice : image
					.getBlockDeviceMappings()) {

				final EbsBlockDevice elasticDevice = blockDevice.getEbs();

				if (elasticDevice == null) {
					continue;
				}

				final String snapshotId = elasticDevice.getSnapshotId();

				if (snapshotId == null) {
					continue;
				}

				compute.tagCreate(snapshotId, amazonTagName(), imageName());

			}

			getLog().info("image create image=\n" + image);

			getLog().info("image create done [" + imageName() + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
