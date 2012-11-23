/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationAsyncClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.google.common.collect.Lists;

/**
 * 
 * @author Andrei Pozolotin
 * 
 * @author Erick Dovale
 *         https://github.com/Syncapse/jenkins-cloudformation-plugin
 */
public class CloudFormation {

	private final Logger logger;

	private final String name;
	private final String template;
	private final List<Parameter> paramList;

	private final long timeout;

	private final AWSCredentials credentials;

	private final AmazonCloudFormation amazonClient;

	private final long waitBetweenAttempts;

	private final String endpoint;

	public CloudFormation(final Logger logger, final String stackName,
			final String stackTemplate, final Map<String, String> stackParams,
			final long timeout, final AWSCredentials credentials,
			final String endpoint) {

		this.logger = logger;

		this.name = stackName;
		this.template = stackTemplate;
		this.paramList = convert(stackParams);

		this.credentials = credentials;

		this.timeout = timeout;

		this.endpoint = endpoint;

		this.waitBetweenAttempts = 10; // query every 10s

		this.amazonClient = newClient(); // keep last

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
	 */
	public Stack stackDelete() throws Exception {

		final DeleteStackRequest request = new DeleteStackRequest();

		request.withStackName(name);

		amazonClient.deleteStack(request);

		final Stack stack = waitForStackDelete();

		return stack;

	}

	/**
	 */
	public Stack stackCreate() throws Exception {

		final CreateStackRequest request = new CreateStackRequest();

		request.withStackName(name);
		request.withParameters(paramList);
		request.withTemplateBody(template);

		amazonClient.createStack(request);

		final Stack stack = waitForStackCreate();

		return stack;

	}

	private AmazonCloudFormation newClient() {

		final AmazonCloudFormation amazonClient = new AmazonCloudFormationAsyncClient(
				credentials);

		logger.info("stack endpoint : {}", endpoint);

		amazonClient.setEndpoint(endpoint);

		return amazonClient;

	}

	private boolean isStackValid(final Stack stack) {
		return stack != null;
	}

	private Stack waitForStackDelete() throws Exception {

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

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

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

	private Stack waitForStackCreate() throws Exception {

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

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case CREATE_IN_PROGRESS:
				final long timeCurrent = System.currentTimeMillis();
				final long timeDiff = timeCurrent - timeStart;
				logger.info("stack create in progress; time=" + timeDiff / 1000);
				sleep();
				continue;
			case CREATE_COMPLETE:
				logger.info("stack create success");
				printStackEvents();
				return stack;
			default:
				logger.error("stack create failure");
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

	private void sleep() throws Exception {
		try {
			Thread.sleep(waitBetweenAttempts * 1000);
		} catch (final InterruptedException ie) {
			throw new IllegalStateException("operation interrupted; "
					+ "resources are left in inconsistent state; "
					+ "requires manual intervention");
		}
	}

	public void logParamList() {

		for (final Parameter param : paramList) {

			logger.info(//
			param.getParameterKey() + "=" + param.getParameterValue());

		}

	}
}
