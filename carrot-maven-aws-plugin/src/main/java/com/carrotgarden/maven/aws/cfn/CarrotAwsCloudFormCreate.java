/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;

/**
 * cloud formation:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/APIReference/API_CreateStack.html"
 * >create stack</a></b>
 * 
 * based on:
 * 
 * <b>stack name</b>,
 * 
 * <b><a href=
 * "http://aws.amazon.com/cloudformation/aws-cloudformation-templates" >stack
 * template</a></b>
 * 
 * ({@link #stackTemplateFile}),
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
 * >stack parameters</a></b>
 * 
 * ({@link #stackPropertiesInputFile} + {@link #stackInputParams}),
 * 
 * and produce a
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/concept-outputs.html"
 * >stack output</a></b>
 * 
 * ({@link #stackPropertiesOutputFile})
 * 
 * ; wait for completion or fail ({@link #stackTimeout});
 * 
 * <p>
 * note: template parameters names starting with "stack" are reserved, see
 * {@link #PREFIX}
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
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://aws.amazon.com/cloudformation/aws-cloudformation-templates"
	 * >template</a>
	 * 
	 * file
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation.template"
	 */
	protected File stackTemplateFile;

	/**
	 * AWS CloudFormation
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
	 * >Parameters Declaration</a>
	 * 
	 * stack template input parameters; optional; overrides settings from
	 * #stackPropertiesInputFile
	 * 
	 * @parameter
	 */
	protected Map<String, String> stackInputParams = new HashMap<String, String>();

	/**
	 * AWS CloudFormation stack create execution
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
	 * >Parameters Declaration</a>
	 * 
	 * input properties file;
	 * 
	 * will be overridden by #stackInputParams if any
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation-input.properties"
	 */
	protected File stackPropertiesInputFile;

	/**
	 * AWS CloudFormation stack create execution result
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/concept-outputs.html"
	 * >Outputs Section</a>
	 * 
	 * output properties file
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation-output.properties"
	 */
	protected File stackPropertiesOutputFile;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		try {

			getLog().info("stack create init [" + stackName + "]");

			final Properties stackInputProps = load(stackPropertiesInputFile);

			final Map<String, String> stackParams = getStackParams(
					stackInputProps, stackInputParams);

			overrideStackParams(stackParams);

			final CloudFormation formation = getCloudFormation(
					stackTemplateFile, stackParams);

			formation.logParamList();

			final Stack stack = formation.stackCreate();

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case CREATE_COMPLETE:
				break;
			default:
				throw new IllegalStateException("stack create failed");
			}

			getLog().info("stack create stack=\n" + stack);

			getLog().info("stack create output:");

			final List<Output> outputList = stack.getOutputs();

			final Properties outputProps = new Properties();

			for (final Output output : outputList) {

				final String key = output.getOutputKey();
				final String value = output.getOutputValue();

				outputProps.put(key, value);

				getLog().info("\t" + key + "=" + value);

			}

			save(outputProps, stackPropertiesOutputFile);

			getLog().info("stack create done [" + stackName + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}
}
