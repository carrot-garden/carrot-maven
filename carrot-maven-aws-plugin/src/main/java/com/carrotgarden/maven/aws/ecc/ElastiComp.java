/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.carrotgarden.maven.aws.CarrotMojo;
import com.carrotgarden.maven.aws.util.AWSCredentialsImpl;

/**
 * base for elastic compute goals
 */
public abstract class ElastiComp extends CarrotMojo {

	/**
	 * AWS ElasticCompute
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
	 * @parameter default-value="com.example.aws.compute"
	 */
	private String computeServerId;

	/**
	 * AWS ElasticCompute operation timeout; seconds
	 * 
	 * @parameter default-value="600"
	 */
	private Long computeTimeout;

	/**
	 * AWS ElasticCompute
	 * 
	 * <a href=
	 * "http://docs.amazonwebservices.com/general/latest/gr/rande.html#ec2_region"
	 * >optional api end point url</a>
	 * 
	 * which controls amazon region selection;
	 * 
	 * when omitted, will be constructed from {@link #computeEndpointFormat} and
	 * {@link #amazonRegion}
	 * 
	 * @parameter
	 */
	private String computeEndpoint;

	/**
	 * AWS ElasticCompute end point format
	 * 
	 * @parameter default-value="https://ec2.%s.amazonaws.com"
	 */
	private String computeEndpointFormat;

	protected String computeEndpoint() {
		return amazonEndpoint(computeEndpoint, computeEndpointFormat);
	}

	protected CarrotElasticCompute newElasticCompute() throws Exception {

		final Server server = settings().getServer(computeServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"server definition is missing for serverId="
							+ computeServerId);
		}

		final AWSCredentials credentials = new AWSCredentialsImpl(server);

		final Logger logger = getLogger(getClass());

		final CarrotElasticCompute compute = new CarrotElasticCompute( //
				logger, //
				computeTimeout, //
				credentials, //
				computeEndpoint() //
		);

		return compute;

	}

}
