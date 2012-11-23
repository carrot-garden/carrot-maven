/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.grv;

import java.util.Map;
import java.util.Properties;

import com.carrotgarden.maven.aws.util.MavenProps;

/**
 * properties proxy:
 * <p>
 * {@link #get(Object)} does hierarchical lookup
 * <p>
 * {@link #put(Object, Object)} updates only project
 */
@SuppressWarnings("serial")
public class MavenPropertiesAdaptor extends Properties {

	public final MavenProps mavenProps;

	public MavenPropertiesAdaptor(final MavenProps mavenProps) {

		this.mavenProps = mavenProps;

		if (mavenProps.isSystem) {
			superPutAll(mavenProps.propsSystem());
		}

		if (mavenProps.isCommand) {
			superPutAll(mavenProps.propsCommand());
		}

		if (mavenProps.isProject) {
			superPutAll(mavenProps.propsProject());
		}

	}

	@Override
	public String getProperty(final String key) {

		final Object value = get(key);

		return value == null ? null : value.toString();

	}

	@Override
	public Object get(final Object key) {

		return superGet(key);

	}

	/** save into both local and project.properties */
	@Override
	public Object put(final Object grvKey, final Object grvValue) {

		final String key = grvKey == null ? null : grvKey.toString();
		final String value = grvValue == null ? null : grvValue.toString();

		superPut(key, value);

		return mavenProps.propsProject().put(key, value);

	}

	private Object superGet(final Object key) {

		return super.get(key);

	}

	private Object superPut(final Object key, final Object value) {

		return super.put(key, value);

	}

	private void superPutAll(final Properties props) {

		for (final Map.Entry<Object, Object> entry : props.entrySet()) {

			superPut(entry.getKey(), entry.getValue());

		}

	}

}
