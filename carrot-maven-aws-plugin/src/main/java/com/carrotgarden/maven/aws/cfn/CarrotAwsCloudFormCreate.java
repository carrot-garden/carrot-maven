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
 * @description create new cloud formation stack based on template
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
	 * cloud formation stack create execution result output properties file
	 * 
	 * @required
	 * @parameter default-value="./target/formation/formation.properties"
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
