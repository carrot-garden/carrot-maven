package com.carrotgarden.maven.aws.dns;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecordSet;

public class Route53 {

	private final AmazonRoute53 amazonClient;

	private final String awsAccessKey;
	private final String awsSecretKey;

	public Route53(final String awsAccessKey, final String awsSecretKey) {

		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;

		this.amazonClient = newClient();

	}

	private AmazonRoute53 newClient() {

		final AWSCredentials credentials = new BasicAWSCredentials(
				this.awsAccessKey, this.awsSecretKey);

		final AmazonRoute53 amazonClient = new AmazonRoute53Client(credentials);

		return amazonClient;

	}

	public HostedZone findZone(final String source) {

		final ListHostedZonesResult zoneResult = amazonClient.listHostedZones();

		final List<HostedZone> zoneList = zoneResult.getHostedZones();

		for (final HostedZone zone : zoneList) {

			final String name = zone.getName();

			if (source.endsWith(name)) {
				return zone;
			}

		}

		return null;

	}

	public ResourceRecordSet findRecord(final String zoneId, final String source) {

		final ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();

		request.setHostedZoneId(zoneId);

		final ListResourceRecordSetsResult result = amazonClient
				.listResourceRecordSets(request);

		final List<ResourceRecordSet> recordList = result
				.getResourceRecordSets();

		for (final ResourceRecordSet record : recordList) {

			final String name = record.getName();

			if (name.equals(source)) {
				return record;
			}

		}

		return null;

	}

	private void assertNotNull(final Object instance, final String message) {
		if (instance == null) {
			throw new IllegalStateException(message);

		}
	}

	public void ensureCNAME(final String source, final String target)
			throws Exception {

		final HostedZone zone = findZone(source);

		assertNotNull(zone, "missing zone for " + source);

		final String zoneId = zone.getId();

		final ResourceRecordSet record = findRecord(zoneId, source);

		if (record == null) {
			createCNAME(source, target);
		} else {
			updateCNAME(source, target);
		}

	}

	public void createCNAME(final String source, final String target)
			throws Exception {

		final HostedZone zone = findZone(source);

		final Change change = new Change();
		change.setAction(ChangeAction.DELETE);
		final ResourceRecordSet record = null;
		change.setResourceRecordSet(record);

		final Collection<Change> changeList = new LinkedList<Change>();

		final ChangeBatch changeRequest = new ChangeBatch();

		changeRequest.setChanges(changeList);

		final ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
		request.setHostedZoneId(zone.getId());
		request.setChangeBatch(changeRequest);

		final ChangeResourceRecordSetsResult result = amazonClient
				.changeResourceRecordSets(request);

		final ChangeInfo changeResult = result.getChangeInfo();

	}

	public void updateCNAME(final String source, final String target)
			throws Exception {

	}

}
