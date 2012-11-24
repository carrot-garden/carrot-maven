/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.cloudformation.model.Stack;

/**
 * cloud formation:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/APIReference/API_DescribeStacks.html"
 * >find stack</a></b>
 * 
 * by name
 * 
 * @goal cloud-formation-find-stack
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CloudFormFindStack extends CloudForm {

	/**
	 * name of project.property which will contain {@link Stack} instance after
	 * execution of this maven goal, which can be used by groovy script as
	 * follows:
	 * 
	 * <pre>
	 * def stack = project.properties["amazonStack"]
	 * println "name = " + stack.stackName
	 * println "status = " + stack.stackStatus
	 * </pre>
	 * 
	 * @required
	 * @parameter default-value="amazonStack"
	 */
	private String stackResultProperty;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("stack find init [" + stackName() + "]");

			final CarrotCloudForm formation = newCloudFormation(null, null);

			final Stack stack = formation.findStack();

			getLog().info("stack find result : " + stack);

			final Properties props = project().getProperties();

			if (stack == null) {
				props.remove(stackResultProperty);
			} else {
				props.put(stackResultProperty, stack);
			}

			getLog().info("stack find done [" + stackName() + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
