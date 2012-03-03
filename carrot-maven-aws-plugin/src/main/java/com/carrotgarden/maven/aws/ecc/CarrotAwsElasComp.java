/**
 * Copyright (C) 2010 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

/**
 */

import org.apache.maven.settings.Server;
import org.slf4j.Logger;

import com.carrotgarden.maven.aws.CarrotAws;

/**
 * 
 */
public abstract class CarrotAwsElasComp extends CarrotAws {

	/**
	 * AWS ElasticCompute login credentials stored in settings.xml under server
	 * id entry; username="aws key id", password="aws secret key";
	 * 
	 * @required
	 * @parameter default-value="com.example.aws.compute"
	 */
	protected String computeServerId;

	/**
	 * AWS ElasticCompute operation timeout; seconds
	 * 
	 * @required
	 * @parameter default-value="600"
	 */
	protected Long computeTimeout;

	protected ElasticCompute getElasticCompute() throws Exception {

		final Server server = settings.getServer(computeServerId);

		if (server == null) {
			throw new IllegalArgumentException(
					"server definition is missing for serverId="
							+ computeServerId);
		}

		final String username = server.getUsername();
		final String password = server.getPassword();

		final Logger logger = getLogger(ElasticCompute.class);

		final ElasticCompute compute = new ElasticCompute(logger, null, null,
				null, computeTimeout, username, password);

		return compute;

	}

}
