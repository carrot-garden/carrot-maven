/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.dns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
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
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.carrotgarden.maven.aws.util.Util;

public class Route53 {

	private final Logger logger;

	private final AmazonRoute53 amazonClient;

	private final AWSCredentials credentials;

	public Route53(final Logger logger, final AWSCredentials credentials) {

		this.logger = logger;

		this.credentials = credentials;

		this.amazonClient = newClient();

	}

	private AmazonRoute53 newClient() {

		final AmazonRoute53 amazonClient = new AmazonRoute53Client(credentials);

		return amazonClient;

	}

	public List<String> listZone(final String source) {

		final List<String> nameList = new LinkedList<String>();

		final HostedZone zone = findZone(source);

		if (zone == null) {
			return nameList;
		}

		final ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();

		request.setHostedZoneId(zone.getId());

		while (true) {

			final ListResourceRecordSetsResult result = amazonClient
					.listResourceRecordSets(request);

			final List<ResourceRecordSet> recordList = result
					.getResourceRecordSets();

			for (final ResourceRecordSet record : recordList) {
				nameList.add(record.getName());
			}

			if (!result.isTruncated()) {
				break;
			}

			request.setStartRecordName(result.getNextRecordName());

		}

		return nameList;

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

	public ResourceRecordSet makeRecordCNAME(final String source,
			final String target) {

		final Collection<ResourceRecord> resourceList = new ArrayList<ResourceRecord>();
		resourceList.add(new ResourceRecord(target));

		final ResourceRecordSet record = new ResourceRecordSet();
		record.setName(source);
		record.setTTL(60L);
		record.setType("CNAME");
		record.setResourceRecords(resourceList);

		return record;
	}

	public String canonical(String name) {

		name = name.toLowerCase();

		if (!name.endsWith(".")) {
			name = name + ".";
		}

		return name;
	}

	public void ensureCNAME(final String source, final String target)
			throws Exception {

		final HostedZone zone = findZone(source);

		Util.assertNotNull(zone, "missing zone for " + source);

		final String zoneId = zone.getId();

		final boolean isPresent;
		final ResourceRecordSet recordOld;
		{
			final ResourceRecordSet recordFound = findRecord(zoneId, source);
			if (recordFound == null) {
				isPresent = false;
				recordOld = makeRecordCNAME(source, target);
			} else {
				isPresent = true;
				recordOld = recordFound;
			}
		}

		final ResourceRecordSet recordNew = makeRecordCNAME(source, target);

		recordNew.setTTL(recordOld.getTTL());

		//

		final Collection<Change> changeList = new LinkedList<Change>();
		if (isPresent) {
			changeList.add(new Change(ChangeAction.DELETE, recordOld));
			changeList.add(new Change(ChangeAction.CREATE, recordNew));
		} else {
			changeList.add(new Change(ChangeAction.CREATE, recordNew));
		}

		final ChangeBatch changeRequest = new ChangeBatch();
		changeRequest.setComment("updated : " + new Date());
		changeRequest.setChanges(changeList);

		final ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
		request.setHostedZoneId(zone.getId());
		request.setChangeBatch(changeRequest);

		final ChangeResourceRecordSetsResult result = amazonClient
				.changeResourceRecordSets(request);

		final ChangeInfo changeResult = result.getChangeInfo();

		logger.info("changeResult : \n{}", changeResult);

	}

}
