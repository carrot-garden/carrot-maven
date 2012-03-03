package com.carrotgarden.maven.aws.cfn;

/**
 */

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
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
 * ({@link #stackInputParams} + {@link #stackPropertiesInputFile}),
 * 
 * and produce a
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/concept-outputs.html"
 * >stack output</a></b>
 * 
 * ({@link #stackPropertiesOutputFile})
 * 
 * ; wait for completion or fail ({@link #stackTimeout})
 * 
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
	 * AWS CloudFormation stack create execution
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html"
	 * >Parameters Declaration</a>
	 * 
	 * input properties file
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

			getLog().info("stack create init");

			final CloudFormation formation = getCloudFormation();

			final Stack stack = formation.stackCreate();

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case CREATE_COMPLETE:
				break;
			default:
				throw new IllegalStateException("create format failed");
			}

			getLog().info("stack create output:");

			final List<Output> outputList = stack.getOutputs();

			final Properties props = new Properties();

			for (final Output output : outputList) {

				final String key = output.getOutputKey();
				final String value = output.getOutputValue();

				props.put(key, value);

				getLog().info("\t" + key + "=" + value);

			}

			save(props);

			getLog().info("stack create done");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

	protected void save(final Properties props) throws Exception {

		getLog().info("stack create output : " + stackPropertiesOutputFile);

		final File folder = stackPropertiesOutputFile.getParentFile();

		if (!folder.exists()) {
			folder.mkdirs();
		}

		final Writer writer = new FileWriter(stackPropertiesOutputFile);

		final String comments = "stack name : " + stackName;

		props.store(writer, comments);

	}

}
