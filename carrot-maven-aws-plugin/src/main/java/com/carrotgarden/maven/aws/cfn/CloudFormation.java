package com.carrotgarden.maven.aws.cfn;

/**
 * 
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationAsyncClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.google.common.collect.Lists;

/**
 * 
 * @author erick dovale
 * 
 *         https://github.com/Syncapse/jenkins-cloudformation-plugin
 * 
 */
public class CloudFormation {

	private final Logger logger;

	private final String name;
	private final String template;
	private final List<Parameter> paramList;

	private final long timeout;

	private final String awsAccessKey;
	private final String awsSecretKey;

	private final AmazonCloudFormation amazonClient;

	// private Stack stack;
	private final long waitBetweenAttempts;

	public CloudFormation(final Logger logger, final String stackName,
			final String stackTemplate, final Map<String, String> stackParams,
			final long timeout, final String awsAccessKey,
			final String awsSecretKey) {

		this.logger = logger;

		this.name = stackName;
		this.template = stackTemplate;
		this.paramList = convert(stackParams);

		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;

		this.timeout = timeout;

		this.amazonClient = getClient();

		this.waitBetweenAttempts = 10; // query every 10s

	}

	private List<Parameter> convert(final Map<String, String> paraMap) {

		final List<Parameter> list = Lists.newArrayList();

		if (paraMap == null || paraMap.values().size() == 0) {
			return list;
		}

		for (final String name : paraMap.keySet()) {
			final Parameter parameter = new Parameter();
			parameter.setParameterKey(name);
			parameter.setParameterValue(paraMap.get(name));
			list.add(parameter);
		}

		return list;

	}

	/**
	 * @return
	 */
	public boolean delete() {

		logger.info("Deleting Cloud Formation stack: " + name);

		final DeleteStackRequest request = new DeleteStackRequest();

		request.withStackName(name);

		amazonClient.deleteStack(request);

		waitForStackDelete();

		logger.info("Cloud Formation stack: " + name + " deleted successfully");

		return true;

	}

	/**
	 * @return A Map containing all outputs or null if creating the stack fails.
	 * 
	 */
	public Map<String, String> create() {

		final Map<String, String> stackOutput = new HashMap<String, String>();

		try {

			logger.info("Creating Cloud Formation stack: " + name);

			final CreateStackRequest request = new CreateStackRequest();

			request.withStackName(name);
			request.withParameters(paramList);
			request.withTemplateBody(template);

			amazonClient.createStack(request);

			final Stack stack = waitForStackCreate();

			if (isStatus(stack, StackStatus.CREATE_COMPLETE)) {

				final List<Output> outputs = stack.getOutputs();

				for (final Output output : outputs) {
					stackOutput.put(output.getOutputKey(),
							output.getOutputValue());
				}

				logger.info("Successfully created stack: " + name);

				return stackOutput;

			} else {

				final String reason = stack.getStackStatusReason();

				logger.error("Failed to create stack: " + name + ". Reason: "
						+ reason);

				return null;

			}

		} catch (final Exception e) {

			logger.error("Failed to create stack: " + name + ". Reason: "
					+ e.getLocalizedMessage());

			return null;

		}

	}

	private AmazonCloudFormation getClient() {

		final AWSCredentials credentials = new BasicAWSCredentials(
				this.awsAccessKey, this.awsSecretKey);

		final AmazonCloudFormation amazonClient = new AmazonCloudFormationAsyncClient(
				credentials);

		return amazonClient;

	}

	private boolean isStackValid(final Stack stack) {
		return stack != null;
	}

	private Stack waitForStackDelete() {

		final long timeStart = System.currentTimeMillis();

		while (true) {

			if (isTimeoutPending(timeStart)) {
				return newStackWithStatus(StackStatus.DELETE_FAILED,
						"stack delete timeout");
			}

			Stack stack = null;
			try {
				stack = getStack();
			} catch (final Exception e) {
				return newStackWithStatus(StackStatus.DELETE_FAILED,
						e.toString());
			}

			if (!isStackValid(stack)) {
				return newStackWithStatus(StackStatus.DELETE_COMPLETE,
						"stack delete invalid/missing");
			}

			final StackStatus status = getStatus(stack);

			switch (status) {
			case DELETE_IN_PROGRESS:
				final long timeCurrent = System.currentTimeMillis();
				final long timeDiff = timeCurrent - timeStart;
				logger.info("stack delete in progress; time=" + timeDiff / 1000);
				sleep();
				continue;
			case DELETE_COMPLETE:
				logger.info("stack delete complete");
				printStackEvents();
				return stack;
			default:
				logger.error("stack delete failed");
				return stack;
			}

		}

	}

	private Stack newStackWithStatus(final StackStatus status,
			final String reason) {

		final Stack stack = new Stack();

		stack.setStackName(name);
		stack.setStackStatus(status);
		stack.setStackStatusReason(reason);

		return stack;

	}

	private Stack waitForStackCreate() {

		final long timeStart = System.currentTimeMillis();

		while (true) {

			if (isTimeoutPending(timeStart)) {
				return newStackWithStatus(StackStatus.CREATE_FAILED,
						"stack create timeout");
			}

			Stack stack = null;
			try {
				stack = getStack();
			} catch (final Exception e) {
				return newStackWithStatus(StackStatus.CREATE_FAILED,
						e.toString());
			}

			if (!isStackValid(stack)) {
				return newStackWithStatus(StackStatus.CREATE_FAILED,
						"stack create invalid/missing");
			}

			final StackStatus status = getStatus(stack);

			switch (status) {
			case CREATE_IN_PROGRESS:
				final long timeCurrent = System.currentTimeMillis();
				final long timeDiff = timeCurrent - timeStart;
				logger.info("stack create in progress; time=" + timeDiff / 1000);
				sleep();
				continue;
			case CREATE_COMPLETE:
				logger.info("stack create complete");
				printStackEvents();
				return stack;
			default:
				return stack;
			}

		}

	}

	private void printStackEvents() {

		final DescribeStackEventsRequest request = new DescribeStackEventsRequest();

		request.withStackName(name);

		final DescribeStackEventsResult describeStackEvents = amazonClient
				.describeStackEvents(request);

		final List<StackEvent> stackEvents = describeStackEvents
				.getStackEvents();

		Collections.reverse(stackEvents);

		logger.info("stack events:");

		for (final StackEvent event : stackEvents) {

			final StringBuilder text = new StringBuilder(128);

			text.append("\n\t");
			text.append("time=");
			text.append(event.getTimestamp());

			text.append("\n\t");
			text.append("id=");
			text.append(event.getEventId());

			text.append("\n\t");
			text.append("type=");
			text.append(event.getResourceType());

			text.append("\n\t");
			text.append("status=");
			text.append(event.getResourceStatus());

			text.append("\n\t");
			text.append("reason=");
			text.append(event.getResourceStatusReason());

			logger.info("event {}", text);

		}

	}

	private boolean isTimeoutPending(final long startTime) {
		return (System.currentTimeMillis() - startTime) > (timeout * 1000);
	}

	private Stack getStack() throws Exception {

		final DescribeStacksRequest request = new DescribeStacksRequest();

		final DescribeStacksResult result = amazonClient
				.describeStacks(request);

		for (final Stack stack : result.getStacks()) {
			if (name.equals(stack.getStackName())) {
				return stack;
			}
		}

		return null;

	}

	private void sleep() {
		try {

			Thread.sleep(waitBetweenAttempts * 1000);

		} catch (final InterruptedException ie) {

			Stack stack = null;
			try {
				stack = getStack();
			} catch (final Exception e) {
				//
			}

			if (stack != null) {

				logger.error("Received an interruption signal. "
						+ "There is a stack created or in the proces of creation. "
						+ "Check in your amazon account to ensure you are not charged for this.");

				logger.info("Stack details: " + stack);

			}

		}

	}

	private boolean isStatus(final Stack stack, final StackStatus status) {
		if (stack == null) {
			return false;
		} else {
			return getStatus(stack) == status;
		}
	}

	private StackStatus getStatus(final Stack stack) {

		final String status = stack.getStackStatus();

		final StackStatus result = StackStatus.fromValue(status);

		return result;

	}

}
