package com.carrotgarden.maven.aws.grv;

import java.util.Properties;

public class MavenProjectAdaptor extends MavenProjectDelegate {

	private final Properties properties;

	public MavenProjectAdaptor(final MavenConfig mavenConfig) {

		super(mavenConfig.project);

		this.properties = new MavenPropertiesAdaptor(mavenConfig);

	}

	@Override
	public Properties getProperties() {

		return properties;

	}

}
