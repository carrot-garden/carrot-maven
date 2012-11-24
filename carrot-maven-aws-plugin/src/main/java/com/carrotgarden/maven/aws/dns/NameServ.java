/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.dns;

import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.carrotgarden.maven.aws.CarrotMojo;
import com.carrotgarden.maven.aws.util.AWSCredentialsImpl;

/**
 * base for route 53 dns goals
 */
public abstract class NameServ extends CarrotMojo {

	/**
	 * AWS Route53
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/AWSSecurityCredentials/1.0/AboutAWSCredentials.html"
	 * >amazon security credentials</a>
	 * 
	 * stored in
	 * 
	 * <a href=
	 * "http://www.sonatype.com/books/mvnref-book/reference/appendix-settings-sect-details.html"
	 * >maven settings.xml</a>
	 * 
	 * under server id entry; username="Access Key ID",
	 * password="Secret Access Key";
	 * 
	 * @required
	 * @parameter default-value="com.example.aws.route53"
	 */
	private String dnsServerId;

	protected CarrotRoute53 newRoute53() throws Exception {

		final Server server = settings().getServer(dnsServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"server definition is missing for serverId=" + dnsServerId);
		}

		final AWSCredentials credentials = new AWSCredentialsImpl(server);

		final Logger logger = getLogger(CarrotRoute53.class);

		final CarrotRoute53 compute = new CarrotRoute53(logger, credentials);

		return compute;

	}

}
