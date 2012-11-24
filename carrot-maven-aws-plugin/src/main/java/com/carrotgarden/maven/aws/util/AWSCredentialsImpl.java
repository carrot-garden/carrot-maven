/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.util;

import org.apache.maven.settings.Server;

import com.amazonaws.auth.AWSCredentials;

/**
 * amazon user/pass from maven settings.xml server entry
 */
public class AWSCredentialsImpl implements AWSCredentials {

	private final String accessKey;
	private final String secretKey;

	public AWSCredentialsImpl(final Server server) {
		this.accessKey = server.getUsername();
		this.secretKey = server.getPassword();
	}

	@Override
	public String getAWSAccessKeyId() {
		return accessKey;
	}

	@Override
	public String getAWSSecretKey() {
		return secretKey;
	}

}
