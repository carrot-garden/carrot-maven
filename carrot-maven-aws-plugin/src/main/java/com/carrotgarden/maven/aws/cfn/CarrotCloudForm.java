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
import java.util.Locale;
import java.util.Map;

import org.apache.maven.settings.Proxy;
import org.slf4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
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
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.google.common.collect.Lists;

/**
 * 
 * @author Andrei Pozolotin
 * 
 * @author Erick Dovale
 *         https://github.com/Syncapse/jenkins-cloudformation-plugin
 */
public class CarrotCloudForm {

	private final AmazonCloudFormation amazonClient;

	private final AWSCredentials credentials;
	private final String endpoint;
	private final Logger logger;

	private final String name;

	private final List<Parameter> paramList;

	private final String template;

	private final long timeout;

	private final long waitBetweenAttempts;

	private final List<String> awsCapabilities;

	public CarrotCloudForm(final Logger logger, final String stackName,
			final String stackTemplate, final Map<String, String> stackParams,
			final long timeout, final AWSCredentials credentials,
			final String endpoint, List<String> awsCapabilities, final List<Proxy> proxies) {

		this.logger = logger;

		this.name = stackName;
		this.template = stackTemplate;
		this.paramList = convert(stackParams);

		this.credentials = credentials;

		this.timeout = timeout;

		this.endpoint = endpoint;
		
		this.awsCapabilities = awsCapabilities;

		this.waitBetweenAttempts = 10; // query every 10s

		this.amazonClient = newClient(proxies); // keep last

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

	public Stack findStack() throws Exception {

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

	private boolean isStackValid(final Stack stack) {
		return stack != null;
	}

	private boolean isTimeoutPending(final long startTime) {
		return (System.currentTimeMillis() - startTime) > (timeout * 1000);
	}

	public void logParamList() {

		for (final Parameter param : paramList) {

			logger.info(//
			param.getParameterKey() + "=" + param.getParameterValue());

		}

	}

	private AmazonCloudFormation newClient(final List<Proxy> proxies) {

		ClientConfiguration config = new ClientConfiguration();
		if (!proxies.isEmpty()) {
			final Proxy proxy = proxies.get(0);
			if (proxies.size() > 1) {
				logger.warn(
						"More than one proxy specified; using the first one [{}]",
						proxy.getHost());
			}
			config.setProxyHost(proxy.getHost());
			config.setProxyPort(proxy.getPort());
			config.setProxyUsername(proxy.getUsername());
			config.setProxyPassword(proxy.getPassword());
			final String protocol = proxy.getProtocol();
			if (null != protocol) {
				config.setProtocol(Protocol.valueOf(protocol
						.toUpperCase(Locale.ENGLISH)));
			}
			/*
			 *  This setup ignores nonProxyHosts, unfortunately. However, the
			 *  target of the HTTP requests is always going to be AWS's servers,
			 *  and you probably wouldn't have specified that in nonProxyHosts anyway.
			 */
		}

		final AmazonCloudFormationAsyncClient amazonClient = new AmazonCloudFormationAsyncClient(
				credentials);
		amazonClient.setConfiguration(config);

		logger.info("stack endpoint : {}", endpoint);

		amazonClient.setEndpoint(endpoint);

		return amazonClient;

	}

	private Stack newStackWithStatus(final StackStatus status,
			final String reason) {

		final Stack stack = new Stack();

		stack.setStackName(name);
		stack.setStackStatus(status);
		stack.setStackStatusReason(reason);

		return stack;

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

	private void sleep() throws Exception {
		try {
			Thread.sleep(waitBetweenAttempts * 1000);
		} catch (final InterruptedException ie) {
			throw new IllegalStateException("operation interrupted; "
					+ "resources are left in inconsistent state; "
					+ "requires manual intervention");
		}
	}

	/**
	 */
	public Stack stackCreate() throws Exception {

		final CreateStackRequest request = new CreateStackRequest();

		request.withStackName(name);
		request.withParameters(paramList);
		request.withTemplateBody(template);
		request.setCapabilities(awsCapabilities);

		amazonClient.createStack(request);

		final Stack stack = waitForStackCreate();

		return stack;

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

	public Stack stackUpdate() throws Exception {

		final UpdateStackRequest request = new UpdateStackRequest();

		request.withStackName(name);
		request.withParameters(paramList);
		request.withTemplateBody(template);

		amazonClient.updateStack(request);

		final Stack stack = waitForStackUpdate();

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
				stack = findStack();
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

	private Stack waitForStackDelete() throws Exception {

		final long timeStart = System.currentTimeMillis();

		while (true) {

			if (isTimeoutPending(timeStart)) {
				return newStackWithStatus(StackStatus.DELETE_FAILED,
						"stack delete timeout");
			}

			Stack stack = null;
			try {
				stack = findStack();
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

	private Stack waitForStackUpdate() throws Exception {

		final long timeStart = System.currentTimeMillis();

		while (true) {

			if (isTimeoutPending(timeStart)) {
				return newStackWithStatus(StackStatus.UPDATE_ROLLBACK_FAILED,
						"stack update timeout");
			}

			Stack stack = null;
			try {
				stack = findStack();
			} catch (final Exception e) {
				return newStackWithStatus(StackStatus.UPDATE_ROLLBACK_FAILED,
						e.toString());
			}

			if (!isStackValid(stack)) {
				return newStackWithStatus(StackStatus.UPDATE_ROLLBACK_FAILED,
						"stack update invalid/missing");
			}

			final StackStatus status = StackStatus.fromValue(stack
					.getStackStatus());

			switch (status) {
			case UPDATE_IN_PROGRESS:
				final long timeCurrent = System.currentTimeMillis();
				final long timeDiff = timeCurrent - timeStart;
				logger.info("stack update in progress; time=" + timeDiff / 1000);
				sleep();
				continue;
			case UPDATE_COMPLETE:
				logger.info("stack update complete");
				printStackEvents();
				return stack;
			default:
				logger.error("stack updtae failed");
				return stack;
			}

		}

	}

}
