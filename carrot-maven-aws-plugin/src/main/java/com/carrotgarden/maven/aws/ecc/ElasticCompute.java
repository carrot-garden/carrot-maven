/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.StateReason;
import com.amazonaws.services.ec2.model.Tag;

/**
 * @author Andrei Pozolotin
 */
public class ElasticCompute {

	private final Logger logger;

	private final String awsAccessKey;
	private final String awsSecretKey;

	private final AmazonEC2 amazonClient;

	private final long timeout;
	private final long waitBetweenAttempts;

	private final String endpoint;

	public ElasticCompute(final Logger logger, final long timeout,
			final String awsAccessKey, final String awsSecretKey,
			final String endpoint) {

		this.logger = logger;

		this.timeout = timeout;

		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;

		this.waitBetweenAttempts = 10;

		this.endpoint = endpoint;

		this.amazonClient = newClient(); // keep last

	}

	private AmazonEC2 newClient() {

		final AWSCredentials credentials = new BasicAWSCredentials(
				this.awsAccessKey, this.awsSecretKey);

		final AmazonEC2 amazonClient = new AmazonEC2Client(credentials);

		amazonClient.setEndpoint(endpoint);

		return amazonClient;

	}

	public void tagCreate(final String resourceId, final String key,
			final String value) {

		final CreateTagsRequest request = new CreateTagsRequest();

		final Collection<String> resourceList = new ArrayList<String>(1);
		resourceList.add(resourceId);

		final Collection<Tag> tagList = new ArrayList<Tag>(1);
		tagList.add(new Tag(key, value));

		request.setResources(resourceList);
		request.setTags(tagList);

		logger.info("tag create request=" + request);

		amazonClient.createTags(request);

	}

	public void tagDelete(final String resourceId, final String key,
			final String value) {

		final DeleteTagsRequest request = new DeleteTagsRequest();

		final Collection<String> resourceList = new ArrayList<String>(1);
		resourceList.add(resourceId);

		final Collection<Tag> tagList = new ArrayList<Tag>(1);
		tagList.add(new Tag(key, value));

		request.setResources(resourceList);
		request.setTags(tagList);

		logger.info("tag delete request=" + request);

		amazonClient.deleteTags(request);

	}

	public Image imageRegister(final String instanceId, final String name,
			final String description) throws Exception {

		final CreateImageRequest request = new CreateImageRequest();

		request.setInstanceId(instanceId);
		request.setName(name);
		request.setDescription(description);

		final CreateImageResult result = amazonClient.createImage(request);

		final String imageId = result.getImageId();

		logger.info("register imageId=" + imageId);

		final Image image = waitForImage(imageId);

		return image;

	}

	public static enum State {

		AVAILABLE("available"), //
		DEREGISTERED("deregistered"), //
		PENDING("pending"), //

		UNKNOWN("unknown"), //

		;

		public final String value;

		State(final String value) {
			this.value = value;
		}

		public static State fromValue(final String value) {
			for (final State known : values()) {
				if (known.value.equals(value)) {
					return known;
				}
			}
			return UNKNOWN;
		}

	}

	private Image newImageWithStatus(final String state, final String code,
			final String message) {

		final StateReason reason = new StateReason();
		reason.setCode(code);
		reason.setMessage(message);

		final Image image = new Image();
		image.setState(state);
		image.setStateReason(reason);

		return image;

	}

	private Image waitForImage(final String imageId) throws Exception {

		final long timeStart = System.currentTimeMillis();

		final List<String> imageIds = new ArrayList<String>();
		imageIds.add(imageId);

		final DescribeImagesRequest request = new DescribeImagesRequest();
		request.setImageIds(imageIds);

		while (true) {

			final DescribeImagesResult result = amazonClient
					.describeImages(request);

			final List<Image> imageList = result.getImages();

			final Image image;

			if (isTimeoutPending(timeStart)) {
				image = newImageWithStatus(State.UNKNOWN.value, "timeout",
						"image create timeout while waiting");
			} else if (imageList == null || imageList.isEmpty()) {
				image = newImageWithStatus(State.UNKNOWN.value, "missing",
						"image create missing in descriptions");
			} else {
				image = imageList.get(0);
			}

			final String value = image.getState();

			final State state = State.fromValue(value);

			switch (state) {

			case AVAILABLE:
				logger.info("image create success");
				return image;

			case PENDING:
				final long timeCurrent = System.currentTimeMillis();
				final long timeDiff = timeCurrent - timeStart;
				logger.info("image create in progress; time=" + timeDiff / 1000);
				sleep();
				break;

			default:
				logger.error("image create failure");
				return image;

			}

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

	private boolean isTimeoutPending(final long startTime) {
		return (System.currentTimeMillis() - startTime) > (timeout * 1000);
	}

}
