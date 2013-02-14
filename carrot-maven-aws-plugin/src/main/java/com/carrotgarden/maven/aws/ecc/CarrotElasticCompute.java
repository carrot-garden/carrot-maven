/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StateReason;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

/**
 * elastic compute controller
 * 
 * @author Andrei Pozolotin
 */
public class CarrotElasticCompute {

	private final Logger logger;

	private final AWSCredentials credentials;

	private final AmazonEC2 amazonClient;

	/** global operation timeout */
	private final long timeout;

	/** time to sleep between steps inside operation, seconds */
	private final long attemptPause;

	/** number of step attempts inside operation before failure */
	private final long attemptCount;

	private final String endpoint;

	public CarrotElasticCompute(final Logger logger, final long timeout,
			final AWSCredentials credentials, final String endpoint) {

		this.logger = logger;

		this.timeout = timeout;

		this.credentials = credentials;

		this.attemptPause = 10; // seconds
		this.attemptCount = 3; // number of times

		this.endpoint = endpoint;

		this.amazonClient = newClient(); // keep last

	}

	private AmazonEC2 newClient() {

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

	public Instance findInstance(final String instanceId) {

		final List<String> instanceIdList = new ArrayList<String>();
		instanceIdList.add(instanceId);

		final DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setInstanceIds(instanceIdList);

		final DescribeInstancesResult result = amazonClient
				.describeInstances(request);

		final List<Reservation> reservationList = result.getReservations();

		switch (reservationList.size()) {
		case 0:
			return null;
		case 1:
			final Reservation reservation = reservationList.get(0);
			final List<Instance> instanceList = reservation.getInstances();
			switch (instanceList.size()) {
			case 0:
				return null;
			case 1:
				return instanceList.get(0);
			default:
				throw new IllegalStateException("duplicate instance");
			}
		default:
			throw new IllegalStateException("duplicate reservation");
		}

	}

	private InstanceStateName stateFrom(final String instanceId) {
		final Instance instance = findInstance(instanceId);
		return InstanceStateName.fromValue(instance.getState().getName());
	}

	private InstanceStateName stateFrom(final Instance instance) {
		return InstanceStateName.fromValue(instance.getState().getName());
	}

	private List<String> wrapList(final String entry) {
		final List<String> list = new ArrayList<String>();
		list.add(entry);
		return list;
	}

	/**
	 * http://shlomoswidler.com/2009/07/ec2-instance-life-cycle.html
	 */
	public void instanceStart(final String instanceId) throws Exception {

		final Instance instance = findInstance(instanceId);

		final InstanceStateName state = stateFrom(instance);

		logger.info("start: current state=" + state);

		switch (state) {
		case Running:
			return;
		case Pending:
			waitForIstanceState(instanceId, InstanceStateName.Running);
			return;
		case Stopped:
			break;
		case Stopping:
			waitForIstanceState(instanceId, InstanceStateName.Stopped);
			break;
		case ShuttingDown:
		case Terminated:
			throw new IllegalStateException("start: dead instance");
		default:
			throw new IllegalStateException("start: unknown state");
		}

		final StartInstancesRequest request = new StartInstancesRequest();
		request.setInstanceIds(wrapList(instanceId));

		final StartInstancesResult result = amazonClient
				.startInstances(request);

		waitForIstanceState(instanceId, InstanceStateName.Running);

	}

	/**
	 * http://shlomoswidler.com/2009/07/ec2-instance-life-cycle.html
	 */
	public void instanceStop(final String instanceId) throws Exception {

		final Instance instance = findInstance(instanceId);

		final InstanceStateName state = stateFrom(instance);

		logger.info("stop: current state=" + state);

		switch (state) {
		case Pending:
			waitForIstanceState(instanceId, InstanceStateName.Running);
		case Running:
			break;
		case Stopping:
			waitForIstanceState(instanceId, InstanceStateName.Stopped);
		case Stopped:
		case Terminated:
		case ShuttingDown:
			return;
		default:
			throw new IllegalStateException("start: unknown state");
		}

		final StopInstancesRequest request = new StopInstancesRequest();
		request.setInstanceIds(wrapList(instanceId));

		final StopInstancesResult result = amazonClient.stopInstances(request);

		waitForIstanceState(instanceId, InstanceStateName.Stopped);

	}

	/**
	 * stop instance and take image snapshot
	 */
	public Image imageCreate(final String instanceId, final String name,
			final String description) throws Exception {

		logger.info("ensure instance state : instanceId=" + instanceId);

		final InstanceStateName state = stateFrom(instanceId);

		final boolean wasRunning;

		switch (state) {
		case Pending:
			waitForIstanceState(instanceId, InstanceStateName.Running);
		case Running:
			wasRunning = true;
			break;
		case Stopping:
			waitForIstanceState(instanceId, InstanceStateName.Stopped);
		case Stopped:
			wasRunning = false;
			break;
		default:
			throw new Exception("image create : invalid instance state="
					+ state);
		}

		if (wasRunning) {
			instanceStop(instanceId);
		}

		final CreateImageRequest request = new CreateImageRequest();

		request.setInstanceId(instanceId);
		request.setName(name);
		request.setDescription(description);

		final CreateImageResult result = amazonClient.createImage(request);

		final String imageId = result.getImageId();

		logger.info("ensure image state: imageId=" + imageId);

		final Image image = waitForImageCreate(imageId);

		if (wasRunning) {
			instanceStart(instanceId);
		}

		return image;

	}

	/**
	 * @return valid image or null if missing
	 */
	public Image findImage(final String imageId) throws Exception {

		/**
		 * work around for image entry not being immediately available right
		 * after create/register operation
		 */
		for (int index = 0; index < attemptCount; index++) {

			try {

				final DescribeImagesRequest request = new DescribeImagesRequest();
				request.setImageIds(wrapList(imageId));

				final DescribeImagesResult result = amazonClient
						.describeImages(request);

				final List<Image> imageList = result.getImages();

				switch (imageList.size()) {
				case 0:
					logger.info("image find : missing imageId=" + imageId);
					break;
				case 1:
					logger.info("image find : success imageId=" + imageId);
					return imageList.get(0);
				default:
					logger.info("image find : duplicate imageId=" + imageId);
					break;
				}

			} catch (final Exception e) {
				logger.info("image find : exception imageId={} / {}", //
						imageId, e.getMessage());
			}

			logger.info("image find : attempt=" + index);

			sleep();

		}

		logger.error("image find : failure imageId=" + imageId);

		return null;

	}

	/** unregister EBS snapshot; will fail if snapshot still in use */
	public void snapshotDelete(final String snapshotId) throws Exception {

		final DeleteSnapshotRequest request = new DeleteSnapshotRequest();
		request.setSnapshotId(snapshotId);

		amazonClient.deleteSnapshot(request);

		logger.info("removed snapshotId = " + snapshotId);

	}

	/** delete AMI image and related EBS snapshots */
	public void imageDelete(final String imageId) throws Exception {

		final Image image = findImage(imageId);

		if (image == null) {
			logger.info("missing imageId = " + imageId);
			return;
		} else {
			logger.info("present imageId = " + imageId);
		}

		imageUnregister(imageId);

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

			snapshotDelete(snapshotId);

		}

		logger.info("removed imageId = " + imageId);

	}

	/** List AMI images matching a given filter and regex. */
	public List<Image> imageList(//
			final String imageFilter, //
			final String imageRegex, //
			final String entrySplit, //
			final String keySplit, //
			final String valueSplit //
	) throws Exception {

		final String[] entryArray = imageFilter.split(entrySplit);

		final List<Filter> filterList = new ArrayList<Filter>();

		for (final String entry : entryArray) {

			final String[] termArray = entry.split(keySplit);

			final String key = termArray[0];
			final String valuesText = termArray[1];

			final String[] valueArray = valuesText.split(valueSplit);

			final Filter filter = new Filter(key, Arrays.asList(valueArray));

			filterList.add(filter);

		}

		final DescribeImagesRequest request = new DescribeImagesRequest();
		request.setFilters(filterList);

		final DescribeImagesResult result = amazonClient
				.describeImages(request);

		final List<Image> resultImages = result.getImages();

		final List<Image> imageList = new ArrayList<Image>();

		final Pattern pattern = Pattern.compile(imageRegex);

		for (final Image image : resultImages) {
			final String search = image.toString();
			if (pattern.matcher(search).matches()) {
				imageList.add(image);
			}
		}

		return imageList;

	}

	public void imageUnregister(final String imageId) throws Exception {

		final DeregisterImageRequest request = new DeregisterImageRequest();
		request.setImageId(imageId);

		amazonClient.deregisterImage(request);

	}

	public static enum ImageState {

		AVAILABLE("available"), //
		DEREGISTERED("deregistered"), //
		PENDING("pending"), //

		UNKNOWN("unknown"), //

		;

		public final String value;

		ImageState(final String value) {
			this.value = value;
		}

		public static ImageState fromValue(final String value) {
			for (final ImageState known : values()) {
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

	private void waitForIstanceState(final String instanceId,
			final InstanceStateName stateName) throws Exception {

		final long timeStart = System.currentTimeMillis();

		while (true) {

			final Instance instance = findInstance(instanceId);

			if (isTimeoutPending(timeStart)) {
				logger.error("instance state : timeout");
				throw new Exception("timeout");
			}

			if (instance == null) {
				logger.error("instance state : missing");
				throw new Exception("missing instance");
			}

			if (stateName == stateFrom(instance)) {
				logger.info("instance state : done");
				break;
			} else {
				final long timeThis = System.currentTimeMillis();
				final long timeDiff = timeThis - timeStart;
				logger.info("instance state; time=" + timeDiff / 1000);
				sleep();
				continue;
			}

		}

	}

	private Image waitForImageCreate(final String imageId) throws Exception {

		if (findImage(imageId) == null) {
			throw new IllegalStateException("image create: missing imageId="
					+ imageId);
		}

		final long timeStart = System.currentTimeMillis();

		final List<String> imageIdList = new ArrayList<String>();
		imageIdList.add(imageId);

		final DescribeImagesRequest request = new DescribeImagesRequest();
		request.setImageIds(imageIdList);

		while (true) {

			final DescribeImagesResult result = amazonClient
					.describeImages(request);

			final List<Image> imageList = result.getImages();

			final Image image;

			if (isTimeoutPending(timeStart)) {
				image = newImageWithStatus(ImageState.UNKNOWN.value, "timeout",
						"image create: timeout while waiting");
			} else if (imageList == null || imageList.isEmpty()) {
				image = newImageWithStatus(ImageState.UNKNOWN.value, "missing",
						"image create: missing in descriptions");
			} else {
				image = imageList.get(0);
			}

			final String value = image.getState();

			final ImageState state = ImageState.fromValue(value);

			switch (state) {

			case AVAILABLE:
				logger.info("image create: success");
				return image;

			case PENDING:
				final long timeThis = System.currentTimeMillis();
				final long timeDiff = timeThis - timeStart;
				logger.info("image create: in progress; time=" + timeDiff
						/ 1000);
				sleep();
				break;

			default:
				logger.error("image create: failure");
				return image;

			}

		}

	}

	private void sleep() throws Exception {
		try {
			Thread.sleep(attemptPause * 1000);
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
