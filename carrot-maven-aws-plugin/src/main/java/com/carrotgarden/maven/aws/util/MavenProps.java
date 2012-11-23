/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.util;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

/**
 * hierarchical maven properties manager
 */
public class MavenProps {

	public final MavenSession session;
	public final MavenProject project;

	public final boolean isSystem;
	public final boolean isCommand;
	public final boolean isProject;

	public MavenProps(//
			final MavenSession session, //
			final MavenProject project, //
			final boolean isSystem,//
			final boolean isCommand, //
			final boolean isProject //
	) {

		this.session = session;
		this.project = project;

		this.isSystem = isSystem;
		this.isCommand = isCommand;
		this.isProject = isProject;

	}

	public Properties propsSystem() {
		return session.getSystemProperties();
	}

	public Properties propsCommand() {
		return session.getUserProperties();
	}

	public Properties propsProject() {
		return project.getProperties();
	}

	public String lookup(final String key) {

		if (isProject) {
			final Object value = propsProject().get(key);
			if (value != null) {
				return value.toString();
			}
		}

		if (isCommand) {
			final Object value = propsCommand().get(key);
			if (value != null) {
				return value.toString();
			}
		}

		if (isSystem) {
			final Object value = propsSystem().get(key);
			if (value != null) {
				return value.toString();
			}
		}

		return null;

	}

	public void putInto(final Properties properties) {

		if (isSystem) {
			properties.putAll(propsSystem());
		}

		if (isCommand) {
			properties.putAll(propsCommand());
		}

		if (isProject) {
			properties.putAll(propsProject());
		}

	}

}
