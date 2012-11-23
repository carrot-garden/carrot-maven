package com.carrotgarden.maven.aws.grv;

import java.util.Map;
import java.util.Properties;

@SuppressWarnings("serial")
public class MavenPropertiesAdaptor extends Properties {

	public final MavenConfig mavenConfig;

	private final Properties propsSystem;
	private final Properties propsCommand;
	private final Properties propsProject;

	public MavenPropertiesAdaptor(final MavenConfig mavenConfig) {

		this.mavenConfig = mavenConfig;

		propsSystem = mavenConfig.session.getSystemProperties();

		propsCommand = mavenConfig.session.getUserProperties();

		propsProject = mavenConfig.project.getProperties();

		if (mavenConfig.isSystem) {
			thisPutAll(propsSystem);
		}

		if (mavenConfig.isCommand) {
			thisPutAll(propsCommand);
		}

		if (mavenConfig.isProject) {
			thisPutAll(propsProject);
		}

	}

	@Override
	public String getProperty(final String key) {

		final Object value = get(key);

		return value == null ? null : value.toString();

	}

	@Override
	public Object get(final Object key) {

		return thisGet(key);

	}

	/** save into both groovy and maven */
	@Override
	public Object put(final Object grvKey, final Object grvValue) {

		final String key = grvKey == null ? null : grvKey.toString();
		final String value = grvValue == null ? null : grvValue.toString();

		thisPut(key, value);

		return propsProject.put(key, value);

	}

	private Object thisGet(final Object key) {

		return super.get(key);

	}

	private Object thisPut(final Object key, final Object value) {

		return super.put(key, value);

	}

	private void thisPutAll(final Properties props) {

		for (final Map.Entry<Object, Object> entry : props.entrySet()) {

			thisPut(entry.getKey(), entry.getValue());

		}

	}

}
