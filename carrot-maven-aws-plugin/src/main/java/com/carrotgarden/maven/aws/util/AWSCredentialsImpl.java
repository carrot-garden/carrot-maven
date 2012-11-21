package com.carrotgarden.maven.aws.util;

import org.apache.maven.settings.Server;

import com.amazonaws.auth.AWSCredentials;

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
